package wiki.label.runner;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import wiki.label.WikiDataQueryService;
import wiki.label.dao.LabelTitleRepo;
import wiki.label.dto.WikiLabelDTO;
import wiki.label.entity.LabelTitleDO;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class QueryTitleRunner implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(QueryTitleRunner.class);

    @Value("${start:0}")
    private Long start = 0L;

    @Resource
    private LabelTitleRepo labelTitleRepo;

    @Resource
    private WikiDataQueryService wikiDataQueryService;

    @Override
    public void run(String... args) throws Exception {
        int num = 1000;

        while (true) {
            Page<LabelTitleDO> labelTitleDOPage = labelTitleRepo.findAllByIdGreaterThanEqual(start, PageRequest.of(0, num));
            logger.info("||| Querying LabelTitle id[{}-{}].... |||", start, start + num);
            start += num;
            List<LabelTitleDO> labelTitleDOList = labelTitleDOPage.getContent();
            List<String> labelList = labelTitleDOList
                    .stream()
                    .filter(d -> StringUtils.isNotEmpty(d.getTitleEn()))
                    .filter(d -> StringUtils.isEmpty(d.getTitleCn()))
                    .map(LabelTitleDO::getLabel)
                    .collect(Collectors.toList());
            List<WikiLabelDTO> wikiLabelDTOList = wikiDataQueryService.listByLabels(labelList);
            logger.info("Fetched {} wiki titles.", wikiLabelDTOList.size());
            compareAndSave(labelTitleDOList, wikiLabelDTOList);

            if (!labelTitleDOPage.hasNext()) {
                break;
            }
        }
        logger.warn("I'm quitting...");
        System.exit(0);

    }

    @Transactional
    public void compareAndSave(List<LabelTitleDO> labelTitleDOList, List<WikiLabelDTO> wikiLabelDTOList) {
        Map<String, WikiLabelDTO> labelDTOMap = new HashMap<>(wikiLabelDTOList.size());
        for (WikiLabelDTO wikiLabelDTO : wikiLabelDTOList) {
            labelDTOMap.put(wikiLabelDTO.getLabel(), wikiLabelDTO);
        }

        List<LabelTitleDO> toSaveTitleDOList = new ArrayList<>();
        for (LabelTitleDO labelTitleDO : labelTitleDOList) {
            String label = labelTitleDO.getLabel();
            if (labelDTOMap.containsKey(label)) {
                WikiLabelDTO wikiLabelDTO = labelDTOMap.get(label);
                // skip too long titles
                if (wikiLabelDTO.getTitleEn() != null && wikiLabelDTO.getTitleEn().length() > 200) {
                    continue;
                }
                if (StringUtils.isEmpty(wikiLabelDTO.getTitleCn())) {
                    continue;
                }
                labelTitleDO.setTitleEn(wikiLabelDTO.getTitleEn());
                labelTitleDO.setTitleEnLower(wikiLabelDTO.getTitleEnLower());
                labelTitleDO.setTitleCn(wikiLabelDTO.getTitleCn());
                toSaveTitleDOList.add(labelTitleDO);
            }
        }

        if (toSaveTitleDOList.size() > 0) {
            logger.info("last in line: {}.", toSaveTitleDOList.get(toSaveTitleDOList.size() - 1));
            labelTitleRepo.saveAll(toSaveTitleDOList);
        }

    }
}

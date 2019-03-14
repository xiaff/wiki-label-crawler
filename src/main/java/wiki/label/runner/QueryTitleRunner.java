package wiki.label.runner;

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
    int cnt = 0;
    while (true) {
      Page<LabelTitleDO> labelTitleDOPage = labelTitleRepo.findAllByIdGreaterThanEqual(start, PageRequest.of(0, num));
      logger.info("||| Querying LabelTitle id[{}-{}].... |||", start, start + num);
      start += num;
      if (!labelTitleDOPage.hasNext()) {
        break;
      }
      List<LabelTitleDO> labelTitleDOList = labelTitleDOPage.getContent();
      List<String> labelList = labelTitleDOList
          .stream()
          .filter(d -> d.getTitleEn() == null)
          .map(LabelTitleDO::getLabel)
          .collect(Collectors.toList());
      List<WikiLabelDTO> wikiLabelDTOList = wikiDataQueryService.listByLabels(labelList);
      logger.info("Fetched {} wiki titles.", wikiLabelDTOList.size());
      compareAndSave(labelTitleDOList, wikiLabelDTOList);
/*      if (++cnt == 10) {
        break;
      }*/
    }
    logger.warn("I'm quitting...");
    System.exit(2);

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
        labelTitleDO.setTitleEn(wikiLabelDTO.getTitleEn());
        labelTitleDO.setTitleEnLower(wikiLabelDTO.getTitleEnLower());
        labelTitleDO.setTitleCn(wikiLabelDTO.getTitleCn());
        toSaveTitleDOList.add(labelTitleDO);
      }
    }

    logger.info("last in line: {}.", toSaveTitleDOList.get(toSaveTitleDOList.size() - 1));
    labelTitleRepo.saveAll(toSaveTitleDOList);

  }
}

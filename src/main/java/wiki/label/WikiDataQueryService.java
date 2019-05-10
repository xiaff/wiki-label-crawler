package wiki.label;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.nlpcn.commons.lang.jianfan.JianFan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import wiki.label.dto.WikiLabelDTO;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class WikiDataQueryService {
  public static final Logger logger = LoggerFactory.getLogger(WikiDataQueryService.class);
  private static final int DEFAULT_QUERY_SIZE = 50;
  private static final String LANG_ZH = "zh";
  private static OkHttpClient CLIENT = new OkHttpClient();
  private static Random RANDOM = new Random();

  private static <T> Collection<List<T>> partition(List<T> list) {
    final AtomicInteger counter = new AtomicInteger(0);

    return list.stream()
        .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / DEFAULT_QUERY_SIZE))
        .values();
  }

  public List<WikiLabelDTO> listByLabels(List<String> labels) throws IOException, InterruptedException {
    List<WikiLabelDTO> wikiLabelDTOList = new ArrayList<>();
    if (CollectionUtils.isEmpty(labels)) {
      return Collections.emptyList();
    }

    for (List<String> subLabelList : partition(labels)) {
      String queryIds = StringUtils.join(subLabelList, "|");
      System.out.println(StringUtils.abbreviate(queryIds, 60));
      List<WikiLabelDTO> wikiLabelDTOS = queryWikiTitle(queryIds);
      if (wikiLabelDTOS == null) {
        // wait for 30s if query failed
        logger.warn("Sleeping for 30 sec...");
        Thread.sleep(1000 * 30);
        wikiLabelDTOS = queryWikiTitle(queryIds);
        if (wikiLabelDTOS == null) {
          wikiLabelDTOS = Collections.emptyList();
        }
      }
      wikiLabelDTOList.addAll(wikiLabelDTOS);
      Thread.sleep((long) (500 * RANDOM.nextDouble()));
    }
    return wikiLabelDTOList;
  }

  private List<WikiLabelDTO> queryWikiTitle(String queryLabelStr) {
    List<WikiLabelDTO> wikiLabelDTOList = new ArrayList<>();

    Request request = new Request.Builder().url("https://www.wikidata.org/w/api.php?action=wbgetentities&props=labels&ids=" + queryLabelStr + "&format=json&languages=en|"+LANG_ZH).get().build();
    try (Response response = CLIENT.newCall(request).execute()) {
      String result = response.body().string();
      JSONObject jsonObject = JSON.parseObject(result);
      boolean success = jsonObject.getBoolean("success");
      if (!success) {
        logger.error("Query unsuccessful: {}", jsonObject);
        return null;
      }
      JSONObject entitiesJson = jsonObject.getJSONObject("entities");
      for (Map.Entry<String, Object> stringObjectEntry : entitiesJson.entrySet()) {
        String mLabel = stringObjectEntry.getKey();
        WikiLabelDTO wikiLabelDTO = new WikiLabelDTO(mLabel);

        JSONObject valueJson = (JSONObject) stringObjectEntry.getValue();
        JSONObject labelsJson = valueJson.getJSONObject("labels");
        if (labelsJson == null) {
          continue;
        }

        if (labelsJson.containsKey("en")) {
          String titleEn = labelsJson.getJSONObject("en").getString("value");
          String titleEnLower = StringUtils.lowerCase(titleEn);
          wikiLabelDTO.setTitleEn(titleEn);
          wikiLabelDTO.setTitleEnLower(titleEnLower);
        }
        if (labelsJson.containsKey(LANG_ZH)) {
          String titleCn = labelsJson.getJSONObject(LANG_ZH).getString("value");
          titleCn = JianFan.f2j(titleCn);
          wikiLabelDTO.setTitleCn(titleCn);
        }
        wikiLabelDTOList.add(wikiLabelDTO);
      }
    } catch (IOException e) {
      logger.warn("Http error: {}.", e.getMessage());
      return null;
    }

    return wikiLabelDTOList;
  }
}

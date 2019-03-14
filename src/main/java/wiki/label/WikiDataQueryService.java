package wiki.label;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import wiki.label.dto.WikiLabelDTO;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class WikiDataQueryService {
  private static final int DEFAULT_QUERY_SIZE = 50;
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
      System.out.println(queryIds);
      List<WikiLabelDTO> wikiLabelDTOS = queryWikiTitle(queryIds);
      wikiLabelDTOList.addAll(wikiLabelDTOS);
      Thread.sleep((long) (1000 * RANDOM.nextDouble()));
    }
    return wikiLabelDTOList;
  }

  private List<WikiLabelDTO> queryWikiTitle(String queryLabelStr) throws IOException {
    List<WikiLabelDTO> wikiLabelDTOList = new ArrayList<>();

    Request request = new Request.Builder().url("https://www.wikidata.org/w/api.php?action=wbgetentities&props=labels&ids=" + queryLabelStr + "&format=json&languages=en|zh-cn").get().build();
    try (Response response = CLIENT.newCall(request).execute()) {
      String result = response.body().string();
//      System.out.println(result);
      JSONObject jsonObject = JSON.parseObject(result);
//      System.out.println(jsonObject);
      boolean success = jsonObject.getBoolean("success");
      if (!success) {
        return Collections.emptyList();
      }
      System.out.println("success: " + success);
      JSONObject entitiesJson = jsonObject.getJSONObject("entities");
      for (Map.Entry<String, Object> stringObjectEntry : entitiesJson.entrySet()) {
        String mLabel = stringObjectEntry.getKey();
        WikiLabelDTO wikiLabelDTO = new WikiLabelDTO(mLabel);

//        System.out.print(mLabel + ": ");
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
//          System.out.print(titleEn + ", " + titleEnLower + ", ");
        }
        if (labelsJson.containsKey("zh-cn")) {
          String titleCn = labelsJson.getJSONObject("zh-cn").getString("value");
          wikiLabelDTO.setTitleCn(titleCn);
//          System.out.println(titleCn);
        }
        wikiLabelDTOList.add(wikiLabelDTO);
      }
    }

    return wikiLabelDTOList;
  }
}

package hieu.dev.chapter9_webCrawler.illness;

import lombok.AllArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public enum CategoryEnum {
    ILLNESS("1", List.of("là gì", "là bệnh gì")),
    SYMPTOM("2", List.of("triệu chứng", "dấu hiệu")),
    CAUSE("3", List.of("nguyên nhân")),
    CURE("4", List.of("điều trị")),
    PREVENTION("5", List.of("phòng chống", "phòng tránh"));
    private final String code;
    private final List<String> descList;

    public static String getCodeByDesc(String description) {
        Optional<CategoryEnum> result = Arrays.stream(CategoryEnum.values()).filter(categoryEnum -> {
            for (String desc : categoryEnum.descList) {
                if(description.toLowerCase().contains(desc)) return true;
            }
            return false;
        }).findFirst();

        return result.map(categoryEnum -> categoryEnum.code).orElse(null);
    }
}

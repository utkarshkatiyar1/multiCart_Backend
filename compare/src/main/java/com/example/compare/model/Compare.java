package com.example.compare.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "compare")
@Data
public class Compare {
    @Id
    private String id;
    private String userId;
    List<Compare_item> compareItems;

    @Data
    public static class Compare_item{
        private String productId;
        private String variantId;
    }
}

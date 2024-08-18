package multiKart.product.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "product")
public class Product {
    @Id
    private String product_id;
    private String title;
    private String description;
    private String type;
    private String brand;
    private List<String> collection;
    private String category;
    private double price;
    private boolean sale;
    private String discount;
    private int stock;
    private boolean newItem;
    private List<String> tags;
    private List<Variant> variants;
    private List<Image> images;
    private String avgRating;

    @Data
    public static class Variant {
        private int variant_id;
        private int id;
        private String sku;
        private String size;
        private String color;
        private int image_id;
        private int  variant_stock_qty;
    }

    @Data
    public static class Image {
        private int image_id;
        private int id;
        private String alt;
        private String src;
        private List<Integer> variant_id;
    }
}

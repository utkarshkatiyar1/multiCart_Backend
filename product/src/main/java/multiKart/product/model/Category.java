package multiKart.product.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "category")

public class Category
{
    @Id
    private String category_id;
    private String slogan;
    private String subCategory;
    private String subTitle;
    private String image;
    private String category;
}

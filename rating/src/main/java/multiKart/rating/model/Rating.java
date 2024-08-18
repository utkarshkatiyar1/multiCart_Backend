package multiKart.rating.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "ratings")
public class Rating
{
    @Id
    private String ratingId;
    private String rating;
    private  String productId;
    private String variantId;
    private String userId;
    private  String comment;

}

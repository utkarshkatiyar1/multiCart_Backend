package multiKart.order.model;
import lombok.Data;
import java.util.List;


@Data
public class ApplicationResponse<T>
{
   private Integer Status;
    private String Message;
    private List<T> Data;

}

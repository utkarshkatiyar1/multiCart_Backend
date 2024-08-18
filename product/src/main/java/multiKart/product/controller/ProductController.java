package multiKart.product.controller;
import multiKart.product.model.ApplicationResponse;
import multiKart.product.model.Product;
import multiKart.product.service.ProductDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/multikart/v1/product")
public class ProductController {
    @Autowired
    ProductDataService productDataService;

    @Operation(summary = "Get all the categories available")
    @Tag(name = "Categories")
    @GetMapping("/allcategories")
    public final ApplicationResponse GetCategory() {
        return productDataService.getCategories();
    }

    @Operation(summary = "Get all the product under category")
    @Tag(name = "Products")
    @GetMapping("/bycategories")
    public final ApplicationResponse getProducts(@RequestParam String category) {
        return productDataService.getProductsByCategory(category);
    }

    @Operation(summary = "Get all the products available")
    @Tag(name = "Products")
    @GetMapping("/all")
    public final List<Product> GetProductsAll() {
        return productDataService.getProductsAll();
    }

    @Operation(summary = "Search the products by keyword in columns title,description,brand ")
    @Tag(name = "Products")
    @GetMapping("/search")
    public final ApplicationResponse searchProductsByName(@RequestParam String keyword) {
        return productDataService.searchProducts(keyword);

    }

    @Operation(summary = "Get the products by it's id")
    @GetMapping("/byid")
    public final ApplicationResponse getProductsById(@RequestParam String id) {
        return productDataService.getProductById(id);
    }


    @GetMapping("/isVariantAvailable") //user in order MS
    public final boolean isVariantIdAvailable(
            @RequestParam String productId,
            @RequestParam int variantId,
            @RequestParam int qty) {
        return productDataService.isVariantIdAvailable(productId, variantId,qty);
    }

    @Operation(summary = "update variant qty for a product after order done") //used in order MS
    @Tag(name = "Products")
    @PutMapping("/updateVariantqty")
    public final ApplicationResponse deleteVariantId(
            @RequestParam String productId,
            @RequestParam int variantId,
            @RequestParam int requestQty) {
        return productDataService.updateVariant(productId, variantId,requestQty);
    }

    @Operation(summary = "Get the products by product's id and varient's id")// created for cart MS to give custom response
    @GetMapping("/byvariantid")
    public final ApplicationResponse getProductsByVariantId(@RequestParam String productId,
                                                            @RequestParam int variantId) {
        return productDataService.getProductByVariantId(productId, variantId);
    }


    @PutMapping("/updateAvgRating")
    public ApplicationResponse updateAvgRating(@RequestBody Product product) {
        return productDataService.updateAvgRating(product.getProduct_id(), product.getAvgRating());
    }
    @Operation(summary = "Get the products filtered based on category, brands, colors, sizes")
    @Tag(name="Products")
    @GetMapping("/filterProducts")
    public ApplicationResponse filterAndSortProducts(@RequestParam(required = false) String category, @RequestParam(required = false) List<String> brands, @RequestParam(required = false) List<String> colors, @RequestParam(required = false) List<String>sizes, @RequestParam(required = false)
            Double minPrice, @RequestParam(required = false) Double maxPrice) {

        return productDataService.filterProducts(category,brands,colors,sizes,minPrice,maxPrice);
    }

}

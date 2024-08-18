package multiKart.product.service;
import multiKart.product.model.ApplicationResponse;
import multiKart.product.model.Product;

import java.util.List;


public interface ProductDataService
{
    ApplicationResponse getCategories();

    ApplicationResponse getProductsByCategory(String category);

    ApplicationResponse<Product> searchProducts(String keyword);

    List<Product> getProductsAll();


    ApplicationResponse getProductById(String id);

    boolean isVariantIdAvailable(String productId, int variantId,int qty);


    ApplicationResponse updateVariant(String productId, int variantId, int requestQty);

    ApplicationResponse getProductByVariantId(String productId, int variantId);
    void saveProduct(Product existingProduct);


    ApplicationResponse updateAvgRating(String productId, String avgRating);

    ApplicationResponse filterProducts( String category, List<String> brands, List<String> colors, List<String>  sizes, Double minPrice, Double maxPrice);
}

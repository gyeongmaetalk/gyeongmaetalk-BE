package auctionTalk.auction.domain.product.repository;

import auctionTalk.auction.domain.product.entity.Product;
import auctionTalk.auction.global.exception.CustomApiException;
import auctionTalk.auction.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    default Product getProduct(Long id) {
        return findProductById(id)
                .orElseThrow(() -> new CustomApiException(ErrorCode.PROPERTY_NOT_FOUND));
    }

    Optional<Product> findProductById(Long id);

    @Query("""
        select distinct p
        from Product p
        join fetch p.componentMappings pcm
        join fetch pcm.component c
        where p.deletedAt is null
        order by p.id asc
    """)
    List<Product> findAllWithComponents();

    Optional<Product> findByStoreProductId(String storeProductId);

}

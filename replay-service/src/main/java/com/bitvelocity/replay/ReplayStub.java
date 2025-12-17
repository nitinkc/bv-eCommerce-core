package com.bitvelocity.replay;

import org.springframework.stereotype.Service;

@Service
public class ReplayStub {
    // Placeholder for replay logic
}
package com.bitvelocity.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    @GetMapping
    public String getProducts() {
        return "Product list (stub)";
    }
}


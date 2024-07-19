package com.lostandfound.demo.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lostandfound.demo.model.Item;
import com.lostandfound.demo.repository.ItemRepository;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    @Autowired
    private ItemRepository itemRepository;

    @PostMapping("/report")
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item) {
        item.setCreatedAt(new Date());
        item.setUpdatedAt(new Date());
        Item savedItem = itemRepository.save(item);
        return ResponseEntity.status(201).body(savedItem);
    }

    @GetMapping("/getItems")
    public ResponseEntity<List<Item>> getItems(@RequestParam(required = false) String item, 
                                               @RequestParam(required = false) String category, 
                                               @RequestParam(required = false) String searchTerm, 
                                               @RequestParam(defaultValue = "0") int startIndex, 
                                               @RequestParam(defaultValue = "99") int limit, 
                                               @RequestParam(defaultValue = "desc") String order) {
        // Implement filtering, sorting, and pagination logic here
        List<Item> items = itemRepository.findAll(); // This should be modified based on filtering and sorting
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemDetails(@PathVariable String id) {
        Item item = itemRepository.findById(id).orElse(null);
        if (item == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(item);
    }

    @PutMapping("/updateItem/{itemId}")
    public ResponseEntity<Item> updateItem(@PathVariable String itemId, @Valid @RequestBody Item itemDetails) {
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            return ResponseEntity.status(404).body(null);
        }
        item.setItem(itemDetails.getItem());
        item.setDateFound(itemDetails.getDateFound());
        item.setLocation(itemDetails.getLocation());
        item.setDescription(itemDetails.getDescription());
        item.setImageUrls(itemDetails.getImageUrls());
        item.setCategory(itemDetails.getCategory());
        item.setDepartment(itemDetails.getDepartment());
        item.setUpdatedAt(new Date());
        Item updatedItem = itemRepository.save(item);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/deleteItem/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable String itemId) {
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            return ResponseEntity.status(404).build();
        }
        itemRepository.delete(item);
        return ResponseEntity.status(200).build();
    }

    @PostMapping("/claim/{itemId}")
    public ResponseEntity<Item> claimItem(@PathVariable String itemId, @RequestBody ClaimItemRequest claimRequest) {
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            return ResponseEntity.status(404).body(null);
        }
        item.setStatus("claimed");
        item.setClaimantName(claimRequest.getName());
        item.setClaimedDate(claimRequest.getDate());
        item.setUpdatedAt(new Date());
        Item updatedItem = itemRepository.save(item);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/getTotalItems")
    public ResponseEntity<TotalItemsResponse> getTotalItems() {
        long totalItems = itemRepository.count();
        long itemsClaimed = itemRepository.countByStatus("claimed");
        long itemsPending = itemRepository.countByStatus("available");

        TotalItemsResponse response = new TotalItemsResponse(totalItems, itemsClaimed, itemsPending);
        return ResponseEntity.ok(response);
    }
}

class ClaimItemRequest {
    private String name;
    private Date date;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

class TotalItemsResponse {
    private long totalItems;
    private long itemsClaimed;
    private long itemsPending;

    public TotalItemsResponse(long totalItems, long itemsClaimed, long itemsPending) {
        this.totalItems = totalItems;
        this.itemsClaimed = itemsClaimed;
        this.itemsPending = itemsPending;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public long getItemsClaimed() {
        return itemsClaimed;
    }

    public void setItemsClaimed(long itemsClaimed) {
        this.itemsClaimed = itemsClaimed;
    }

    public long getItemsPending() {
        return itemsPending;
    }

    public void setItemsPending(long itemsPending) {
        this.itemsPending = itemsPending;
    }

}

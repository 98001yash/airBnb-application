package com.company.projects.airBnbApp.service.Impl;


import com.company.projects.airBnbApp.entity.Hotel;
import com.company.projects.airBnbApp.entity.HotelMinPrice;
import com.company.projects.airBnbApp.entity.Inventory;
import com.company.projects.airBnbApp.repository.HotelMinPriceRepository;
import com.company.projects.airBnbApp.repository.HotelRepository;
import com.company.projects.airBnbApp.repository.InventoryRepository;
import com.company.projects.airBnbApp.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateDService {

    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;


    @Scheduled(cron = "0 0 * * * *")
    public void updatePrices() {
        int page = 0;
        int batchSize = 100;

        while(true){
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page,batchSize));
            if(hotelPage.isEmpty()){
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrices);
            page++;
        }
    }


    private void updateHotelPrices(Hotel hotel){
        log.info("Updating hotel prices for hotel ID: {}",hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);

        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel,startDate,endDate);

        updateInventoryPrices(inventoryList);
        updateHotelMinPrice(hotel,inventoryList,startDate,endDate);
    }

    private void updateHotelMinPrice(Hotel hotel,List<Inventory> inventoryList, LocalDate startDate, LocalDate endDate){
        Map<LocalDate, BigDecimal> dailyMinPrices = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice,Collectors.minBy(Comparator.naturalOrder()))

                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e->e.getValue().orElse(BigDecimal.ZERO)));

        //Prepare HotelPrice entities in bulk
        List<HotelMinPrice> hotelPrices  =new ArrayList<>();
        dailyMinPrices.forEach((date,price)->{
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel,date)
                    .orElse(new HotelMinPrice(hotel,date));
            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        });

        // save all hotelPrices entities in bulk

        hotelMinPriceRepository.saveAll(hotelPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventoryList){
        inventoryList.forEach(inventory->{
            BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
            inventory.setPrice(dynamicPrice);
        });

        inventoryRepository.saveAll(inventoryList);
    }
}

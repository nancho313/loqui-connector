package com.nancho313.loqui.connector.domain.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.nancho313.loqui.commons.ObjectValidator.isNotNull;
import static com.nancho313.loqui.commons.ObjectValidator.isNull;

public record CurrentDate(LocalDateTime creationDate, LocalDateTime lastUpdatedDate) {
  
  public CurrentDate {
    
    List<String> errors = new ArrayList<>();
    
    if (isNull(creationDate)) {
      
      errors.add("The creation date cannot be null.");
    }
    
    if (isNull(lastUpdatedDate)) {
      
      errors.add("The last updated date cannot be null.");
    }
    
    if (isNotNull(creationDate) && isNotNull(lastUpdatedDate) && creationDate.isAfter(lastUpdatedDate)) {
      
      errors.add("The creation date cannot be greater than last updated date.");
    }
    
    if (!errors.isEmpty()) {
      
      throw new IllegalArgumentException("Cannot create a CurrentDate object. Errors -> %s".formatted(errors));
    }
  }
  
  public static CurrentDate now() {
    
    return new CurrentDate(LocalDateTime.now(), LocalDateTime.now());
  }
  
  public CurrentDate update() {
    
    return new CurrentDate(this.creationDate, LocalDateTime.now());
  }
}

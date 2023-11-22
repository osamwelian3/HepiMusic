package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Album type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Albums", type = Model.Type.USER, version = 1)
@Index(name = "undefined", fields = {"key"})
public final class Album implements Model {
  public static final QueryField KEY = field("Album", "key");
  public static final QueryField NAME = field("Album", "name");
  public static final QueryField THUMBNAIL = field("Album", "thumbnail");
  public static final QueryField THUMBNAIL_KEY = field("Album", "thumbnailKey");
  private final @ModelField(targetType="String", isRequired = true) String key;
  private final @ModelField(targetType="String", isRequired = true) String name;
  private final @ModelField(targetType="String") String thumbnail;
  private final @ModelField(targetType="String") String thumbnailKey;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  public String resolveIdentifier() {
    return key;
  }
  
  public String getKey() {
      return key;
  }
  
  public String getName() {
      return name;
  }
  
  public String getThumbnail() {
      return thumbnail;
  }
  
  public String getThumbnailKey() {
      return thumbnailKey;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Album(String key, String name, String thumbnail, String thumbnailKey) {
    this.key = key;
    this.name = name;
    this.thumbnail = thumbnail;
    this.thumbnailKey = thumbnailKey;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Album album = (Album) obj;
      return ObjectsCompat.equals(getKey(), album.getKey()) &&
              ObjectsCompat.equals(getName(), album.getName()) &&
              ObjectsCompat.equals(getThumbnail(), album.getThumbnail()) &&
              ObjectsCompat.equals(getThumbnailKey(), album.getThumbnailKey()) &&
              ObjectsCompat.equals(getCreatedAt(), album.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), album.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getKey())
      .append(getName())
      .append(getThumbnail())
      .append(getThumbnailKey())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Album {")
      .append("key=" + String.valueOf(getKey()) + ", ")
      .append("name=" + String.valueOf(getName()) + ", ")
      .append("thumbnail=" + String.valueOf(getThumbnail()) + ", ")
      .append("thumbnailKey=" + String.valueOf(getThumbnailKey()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static KeyStep builder() {
      return new Builder();
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(key,
      name,
      thumbnail,
      thumbnailKey);
  }
  public interface KeyStep {
    NameStep key(String key);
  }
  

  public interface NameStep {
    BuildStep name(String name);
  }
  

  public interface BuildStep {
    Album build();
    BuildStep thumbnail(String thumbnail);
    BuildStep thumbnailKey(String thumbnailKey);
  }
  

  public static class Builder implements KeyStep, NameStep, BuildStep {
    private String key;
    private String name;
    private String thumbnail;
    private String thumbnailKey;
    @Override
     public Album build() {
        
        return new Album(
          key,
          name,
          thumbnail,
          thumbnailKey);
    }
    
    @Override
     public NameStep key(String key) {
        Objects.requireNonNull(key);
        this.key = key;
        return this;
    }
    
    @Override
     public BuildStep name(String name) {
        Objects.requireNonNull(name);
        this.name = name;
        return this;
    }
    
    @Override
     public BuildStep thumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }
    
    @Override
     public BuildStep thumbnailKey(String thumbnailKey) {
        this.thumbnailKey = thumbnailKey;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String key, String name, String thumbnail, String thumbnailKey) {
      super.key(key)
        .name(name)
        .thumbnail(thumbnail)
        .thumbnailKey(thumbnailKey);
    }
    
    @Override
     public CopyOfBuilder key(String key) {
      return (CopyOfBuilder) super.key(key);
    }
    
    @Override
     public CopyOfBuilder name(String name) {
      return (CopyOfBuilder) super.name(name);
    }
    
    @Override
     public CopyOfBuilder thumbnail(String thumbnail) {
      return (CopyOfBuilder) super.thumbnail(thumbnail);
    }
    
    @Override
     public CopyOfBuilder thumbnailKey(String thumbnailKey) {
      return (CopyOfBuilder) super.thumbnailKey(thumbnailKey);
    }
  }
  
}

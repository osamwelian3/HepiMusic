package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Song type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Songs", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "cognito:username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
  @AuthRule(allow = AuthStrategy.PRIVATE, operations = { ModelOperation.READ })
})
@Index(name = "undefined", fields = {"key"})
public final class Song implements Model {
  public static final QueryField KEY = field("Song", "key");
  public static final QueryField FILE_URL = field("Song", "fileUrl");
  public static final QueryField FILE_KEY = field("Song", "fileKey");
  public static final QueryField LISTENS = field("Song", "listens");
  public static final QueryField TRENDING_LISTENS = field("Song", "trendingListens");
  public static final QueryField LIST_OF_UID_DOWN_VOTES = field("Song", "listOfUidDownVotes");
  public static final QueryField LIST_OF_UID_UP_VOTES = field("Song", "listOfUidUpVotes");
  public static final QueryField NAME = field("Song", "name");
  public static final QueryField PART_OF = field("Song", "partOf");
  public static final QueryField SELECTED_CATEGORY = field("Song", "selectedCategory");
  public static final QueryField SELECTED_CREATOR = field("Song", "selectedCreator");
  public static final QueryField THUMBNAIL = field("Song", "thumbnail");
  public static final QueryField THUMBNAIL_KEY = field("Song", "thumbnailKey");
  public static final QueryField OWNER = field("Song", "owner");
  private final @ModelField(targetType="String", isRequired = true) String key;
  private final @ModelField(targetType="String", isRequired = true) String fileUrl;
  private final @ModelField(targetType="String", isRequired = true) String fileKey;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "cognito:username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ, ModelOperation.UPDATE }),
    @AuthRule(allow = AuthStrategy.PRIVATE, operations = { ModelOperation.READ, ModelOperation.UPDATE })
  }) List<String> listens;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "cognito:username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ, ModelOperation.UPDATE }),
    @AuthRule(allow = AuthStrategy.PRIVATE, operations = { ModelOperation.READ, ModelOperation.UPDATE })
  }) List<String> trendingListens;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "cognito:username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ, ModelOperation.UPDATE }),
    @AuthRule(allow = AuthStrategy.PRIVATE, operations = { ModelOperation.READ, ModelOperation.UPDATE })
  }) List<String> listOfUidDownVotes;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "cognito:username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ, ModelOperation.UPDATE }),
    @AuthRule(allow = AuthStrategy.PRIVATE, operations = { ModelOperation.READ, ModelOperation.UPDATE })
  }) List<String> listOfUidUpVotes;
  private final @ModelField(targetType="String", isRequired = true) String name;
  private final @ModelField(targetType="String") String partOf;
  private final @ModelField(targetType="String", isRequired = true) String selectedCategory;
  private final @ModelField(targetType="String") String selectedCreator;
  private final @ModelField(targetType="String", isRequired = true) String thumbnail;
  private final @ModelField(targetType="String", isRequired = true) String thumbnailKey;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "cognito:username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.DELETE }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, operations = { ModelOperation.READ })
  }) String owner;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  public String resolveIdentifier() {
    return key;
  }
  
  public String getKey() {
      return key;
  }
  
  public String getFileUrl() {
      return fileUrl;
  }
  
  public String getFileKey() {
      return fileKey;
  }
  
  public List<String> getListens() {
      return listens;
  }
  
  public List<String> getTrendingListens() {
      return trendingListens;
  }
  
  public List<String> getListOfUidDownVotes() {
      return listOfUidDownVotes;
  }
  
  public List<String> getListOfUidUpVotes() {
      return listOfUidUpVotes;
  }
  
  public String getName() {
      return name;
  }
  
  public String getPartOf() {
      return partOf;
  }
  
  public String getSelectedCategory() {
      return selectedCategory;
  }
  
  public String getSelectedCreator() {
      return selectedCreator;
  }
  
  public String getThumbnail() {
      return thumbnail;
  }
  
  public String getThumbnailKey() {
      return thumbnailKey;
  }
  
  public String getOwner() {
      return owner;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Song(String key, String fileUrl, String fileKey, List<String> listens, List<String> trendingListens, List<String> listOfUidDownVotes, List<String> listOfUidUpVotes, String name, String partOf, String selectedCategory, String selectedCreator, String thumbnail, String thumbnailKey, String owner) {
    this.key = key;
    this.fileUrl = fileUrl;
    this.fileKey = fileKey;
    this.listens = listens;
    this.trendingListens = trendingListens;
    this.listOfUidDownVotes = listOfUidDownVotes;
    this.listOfUidUpVotes = listOfUidUpVotes;
    this.name = name;
    this.partOf = partOf;
    this.selectedCategory = selectedCategory;
    this.selectedCreator = selectedCreator;
    this.thumbnail = thumbnail;
    this.thumbnailKey = thumbnailKey;
    this.owner = owner;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Song song = (Song) obj;
      return ObjectsCompat.equals(getKey(), song.getKey()) &&
              ObjectsCompat.equals(getFileUrl(), song.getFileUrl()) &&
              ObjectsCompat.equals(getFileKey(), song.getFileKey()) &&
              ObjectsCompat.equals(getListens(), song.getListens()) &&
              ObjectsCompat.equals(getTrendingListens(), song.getTrendingListens()) &&
              ObjectsCompat.equals(getListOfUidDownVotes(), song.getListOfUidDownVotes()) &&
              ObjectsCompat.equals(getListOfUidUpVotes(), song.getListOfUidUpVotes()) &&
              ObjectsCompat.equals(getName(), song.getName()) &&
              ObjectsCompat.equals(getPartOf(), song.getPartOf()) &&
              ObjectsCompat.equals(getSelectedCategory(), song.getSelectedCategory()) &&
              ObjectsCompat.equals(getSelectedCreator(), song.getSelectedCreator()) &&
              ObjectsCompat.equals(getThumbnail(), song.getThumbnail()) &&
              ObjectsCompat.equals(getThumbnailKey(), song.getThumbnailKey()) &&
              ObjectsCompat.equals(getOwner(), song.getOwner()) &&
              ObjectsCompat.equals(getCreatedAt(), song.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), song.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getKey())
      .append(getFileUrl())
      .append(getFileKey())
      .append(getListens())
      .append(getTrendingListens())
      .append(getListOfUidDownVotes())
      .append(getListOfUidUpVotes())
      .append(getName())
      .append(getPartOf())
      .append(getSelectedCategory())
      .append(getSelectedCreator())
      .append(getThumbnail())
      .append(getThumbnailKey())
      .append(getOwner())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Song {")
      .append("key=" + String.valueOf(getKey()) + ", ")
      .append("fileUrl=" + String.valueOf(getFileUrl()) + ", ")
      .append("fileKey=" + String.valueOf(getFileKey()) + ", ")
      .append("listens=" + String.valueOf(getListens()) + ", ")
      .append("trendingListens=" + String.valueOf(getTrendingListens()) + ", ")
      .append("listOfUidDownVotes=" + String.valueOf(getListOfUidDownVotes()) + ", ")
      .append("listOfUidUpVotes=" + String.valueOf(getListOfUidUpVotes()) + ", ")
      .append("name=" + String.valueOf(getName()) + ", ")
      .append("partOf=" + String.valueOf(getPartOf()) + ", ")
      .append("selectedCategory=" + String.valueOf(getSelectedCategory()) + ", ")
      .append("selectedCreator=" + String.valueOf(getSelectedCreator()) + ", ")
      .append("thumbnail=" + String.valueOf(getThumbnail()) + ", ")
      .append("thumbnailKey=" + String.valueOf(getThumbnailKey()) + ", ")
      .append("owner=" + String.valueOf(getOwner()) + ", ")
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
      fileUrl,
      fileKey,
      listens,
      trendingListens,
      listOfUidDownVotes,
      listOfUidUpVotes,
      name,
      partOf,
      selectedCategory,
      selectedCreator,
      thumbnail,
      thumbnailKey,
      owner);
  }
  public interface KeyStep {
    FileUrlStep key(String key);
  }
  

  public interface FileUrlStep {
    FileKeyStep fileUrl(String fileUrl);
  }
  

  public interface FileKeyStep {
    NameStep fileKey(String fileKey);
  }
  

  public interface NameStep {
    SelectedCategoryStep name(String name);
  }
  

  public interface SelectedCategoryStep {
    ThumbnailStep selectedCategory(String selectedCategory);
  }
  

  public interface ThumbnailStep {
    ThumbnailKeyStep thumbnail(String thumbnail);
  }
  

  public interface ThumbnailKeyStep {
    BuildStep thumbnailKey(String thumbnailKey);
  }
  

  public interface BuildStep {
    Song build();
    BuildStep listens(List<String> listens);
    BuildStep trendingListens(List<String> trendingListens);
    BuildStep listOfUidDownVotes(List<String> listOfUidDownVotes);
    BuildStep listOfUidUpVotes(List<String> listOfUidUpVotes);
    BuildStep partOf(String partOf);
    BuildStep selectedCreator(String selectedCreator);
    BuildStep owner(String owner);
  }
  

  public static class Builder implements KeyStep, FileUrlStep, FileKeyStep, NameStep, SelectedCategoryStep, ThumbnailStep, ThumbnailKeyStep, BuildStep {
    private String key;
    private String fileUrl;
    private String fileKey;
    private String name;
    private String selectedCategory;
    private String thumbnail;
    private String thumbnailKey;
    private List<String> listens;
    private List<String> trendingListens;
    private List<String> listOfUidDownVotes;
    private List<String> listOfUidUpVotes;
    private String partOf;
    private String selectedCreator;
    private String owner;
    @Override
     public Song build() {
        
        return new Song(
          key,
          fileUrl,
          fileKey,
          listens,
          trendingListens,
          listOfUidDownVotes,
          listOfUidUpVotes,
          name,
          partOf,
          selectedCategory,
          selectedCreator,
          thumbnail,
          thumbnailKey,
          owner);
    }
    
    @Override
     public FileUrlStep key(String key) {
        Objects.requireNonNull(key);
        this.key = key;
        return this;
    }
    
    @Override
     public FileKeyStep fileUrl(String fileUrl) {
        Objects.requireNonNull(fileUrl);
        this.fileUrl = fileUrl;
        return this;
    }
    
    @Override
     public NameStep fileKey(String fileKey) {
        Objects.requireNonNull(fileKey);
        this.fileKey = fileKey;
        return this;
    }
    
    @Override
     public SelectedCategoryStep name(String name) {
        Objects.requireNonNull(name);
        this.name = name;
        return this;
    }
    
    @Override
     public ThumbnailStep selectedCategory(String selectedCategory) {
        Objects.requireNonNull(selectedCategory);
        this.selectedCategory = selectedCategory;
        return this;
    }
    
    @Override
     public ThumbnailKeyStep thumbnail(String thumbnail) {
        Objects.requireNonNull(thumbnail);
        this.thumbnail = thumbnail;
        return this;
    }
    
    @Override
     public BuildStep thumbnailKey(String thumbnailKey) {
        Objects.requireNonNull(thumbnailKey);
        this.thumbnailKey = thumbnailKey;
        return this;
    }
    
    @Override
     public BuildStep listens(List<String> listens) {
        this.listens = listens;
        return this;
    }
    
    @Override
     public BuildStep trendingListens(List<String> trendingListens) {
        this.trendingListens = trendingListens;
        return this;
    }
    
    @Override
     public BuildStep listOfUidDownVotes(List<String> listOfUidDownVotes) {
        this.listOfUidDownVotes = listOfUidDownVotes;
        return this;
    }
    
    @Override
     public BuildStep listOfUidUpVotes(List<String> listOfUidUpVotes) {
        this.listOfUidUpVotes = listOfUidUpVotes;
        return this;
    }
    
    @Override
     public BuildStep partOf(String partOf) {
        this.partOf = partOf;
        return this;
    }
    
    @Override
     public BuildStep selectedCreator(String selectedCreator) {
        this.selectedCreator = selectedCreator;
        return this;
    }
    
    @Override
     public BuildStep owner(String owner) {
        this.owner = owner;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String key, String fileUrl, String fileKey, List<String> listens, List<String> trendingListens, List<String> listOfUidDownVotes, List<String> listOfUidUpVotes, String name, String partOf, String selectedCategory, String selectedCreator, String thumbnail, String thumbnailKey, String owner) {
      super.key(key)
        .fileUrl(fileUrl)
        .fileKey(fileKey)
        .name(name)
        .selectedCategory(selectedCategory)
        .thumbnail(thumbnail)
        .thumbnailKey(thumbnailKey)
        .listens(listens)
        .trendingListens(trendingListens)
        .listOfUidDownVotes(listOfUidDownVotes)
        .listOfUidUpVotes(listOfUidUpVotes)
        .partOf(partOf)
        .selectedCreator(selectedCreator)
        .owner(owner);
    }
    
    @Override
     public CopyOfBuilder key(String key) {
      return (CopyOfBuilder) super.key(key);
    }
    
    @Override
     public CopyOfBuilder fileUrl(String fileUrl) {
      return (CopyOfBuilder) super.fileUrl(fileUrl);
    }
    
    @Override
     public CopyOfBuilder fileKey(String fileKey) {
      return (CopyOfBuilder) super.fileKey(fileKey);
    }
    
    @Override
     public CopyOfBuilder name(String name) {
      return (CopyOfBuilder) super.name(name);
    }
    
    @Override
     public CopyOfBuilder selectedCategory(String selectedCategory) {
      return (CopyOfBuilder) super.selectedCategory(selectedCategory);
    }
    
    @Override
     public CopyOfBuilder thumbnail(String thumbnail) {
      return (CopyOfBuilder) super.thumbnail(thumbnail);
    }
    
    @Override
     public CopyOfBuilder thumbnailKey(String thumbnailKey) {
      return (CopyOfBuilder) super.thumbnailKey(thumbnailKey);
    }
    
    @Override
     public CopyOfBuilder listens(List<String> listens) {
      return (CopyOfBuilder) super.listens(listens);
    }
    
    @Override
     public CopyOfBuilder trendingListens(List<String> trendingListens) {
      return (CopyOfBuilder) super.trendingListens(trendingListens);
    }
    
    @Override
     public CopyOfBuilder listOfUidDownVotes(List<String> listOfUidDownVotes) {
      return (CopyOfBuilder) super.listOfUidDownVotes(listOfUidDownVotes);
    }
    
    @Override
     public CopyOfBuilder listOfUidUpVotes(List<String> listOfUidUpVotes) {
      return (CopyOfBuilder) super.listOfUidUpVotes(listOfUidUpVotes);
    }
    
    @Override
     public CopyOfBuilder partOf(String partOf) {
      return (CopyOfBuilder) super.partOf(partOf);
    }
    
    @Override
     public CopyOfBuilder selectedCreator(String selectedCreator) {
      return (CopyOfBuilder) super.selectedCreator(selectedCreator);
    }
    
    @Override
     public CopyOfBuilder owner(String owner) {
      return (CopyOfBuilder) super.owner(owner);
    }
  }
  
}

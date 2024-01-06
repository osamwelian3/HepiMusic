package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.annotations.BelongsTo;
import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.core.model.ModelIdentifier;

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

/** This is an auto generated class representing the RequestSong type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "RequestSongs", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
  @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE, ModelOperation.DELETE })
})
@Index(name = "undefined", fields = {"key"})
public final class RequestSong implements Model {
  public static final QueryField KEY = field("RequestSong", "key");
  public static final QueryField FILE_URL = field("RequestSong", "fileUrl");
  public static final QueryField FILE_KEY = field("RequestSong", "fileKey");
  public static final QueryField LISTENS = field("RequestSong", "listens");
  public static final QueryField TRENDING_LISTENS = field("RequestSong", "trendingListens");
  public static final QueryField LIST_OF_UID_DOWN_VOTES = field("RequestSong", "listOfUidDownVotes");
  public static final QueryField LIST_OF_UID_UP_VOTES = field("RequestSong", "listOfUidUpVotes");
  public static final QueryField REQUESTS = field("RequestSong", "requests");
  public static final QueryField REQUEST_UP_VOTES = field("RequestSong", "requestUpVotes");
  public static final QueryField NAME = field("RequestSong", "name");
  public static final QueryField PART_OF = field("RequestSong", "partOf");
  public static final QueryField SELECTED_CATEGORY = field("RequestSong", "selectedCategory");
  public static final QueryField SELECTED_CREATOR = field("RequestSong", "selectedCreator");
  public static final QueryField THUMBNAIL = field("RequestSong", "thumbnail");
  public static final QueryField THUMBNAIL_KEY = field("RequestSong", "thumbnailKey");
  public static final QueryField PLAYLIST = field("RequestSong", "requestPlaylistSongsKey");
  public static final QueryField CREATED_DATE = field("RequestSong", "createdDate");
  public static final QueryField UPDATED_DATE = field("RequestSong", "updatedDate");
  public static final QueryField OWNER_DATA = field("RequestSong", "ownerData");
  public static final QueryField OWNER = field("RequestSong", "owner");
  private final @ModelField(targetType="ID", isRequired = true, authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) String key;
  private final @ModelField(targetType="String", isRequired = true, authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) String fileUrl;
  private final @ModelField(targetType="String", isRequired = true, authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) String fileKey;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ, ModelOperation.UPDATE }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE })
  }) List<String> listens;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ, ModelOperation.UPDATE }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE })
  }) List<String> trendingListens;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE })
  }) List<String> listOfUidDownVotes;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE })
  }) List<String> listOfUidUpVotes;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE })
  }) List<String> requests;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE })
  }) List<String> requestUpVotes;
  private final @ModelField(targetType="String", isRequired = true, authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) String name;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) String partOf;
  private final @ModelField(targetType="String", isRequired = true, authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) String selectedCategory;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) String selectedCreator;
  private final @ModelField(targetType="String", isRequired = true, authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) String thumbnail;
  private final @ModelField(targetType="String", isRequired = true, authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) String thumbnailKey;
  private final @ModelField(targetType="RequestPlaylist", isRequired = true, authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE })
  }) @BelongsTo(targetName = "requestPlaylistSongsKey", targetNames = {"requestPlaylistSongsKey"}, type = RequestPlaylist.class) RequestPlaylist playlist;
  private final @ModelField(targetType="AWSDateTime", isRequired = true, authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.DELETE }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) Temporal.DateTime createdDate;
  private final @ModelField(targetType="AWSDateTime", isRequired = true, authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE, ModelOperation.DELETE }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE })
  }) Temporal.DateTime updatedDate;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE, ModelOperation.DELETE }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) String ownerData;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.DELETE }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) String owner;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  /** @deprecated This API is internal to Amplify and should not be used. */
  @Deprecated
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
  
  public List<String> getRequests() {
      return requests;
  }
  
  public List<String> getRequestUpVotes() {
      return requestUpVotes;
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
  
  public RequestPlaylist getPlaylist() {
      return playlist;
  }
  
  public Temporal.DateTime getCreatedDate() {
      return createdDate;
  }
  
  public Temporal.DateTime getUpdatedDate() {
      return updatedDate;
  }
  
  public String getOwnerData() {
      return ownerData;
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
  
  private RequestSong(String key, String fileUrl, String fileKey, List<String> listens, List<String> trendingListens, List<String> listOfUidDownVotes, List<String> listOfUidUpVotes, List<String> requests, List<String> requestUpVotes, String name, String partOf, String selectedCategory, String selectedCreator, String thumbnail, String thumbnailKey, RequestPlaylist playlist, Temporal.DateTime createdDate, Temporal.DateTime updatedDate, String ownerData, String owner) {
    this.key = key;
    this.fileUrl = fileUrl;
    this.fileKey = fileKey;
    this.listens = listens;
    this.trendingListens = trendingListens;
    this.listOfUidDownVotes = listOfUidDownVotes;
    this.listOfUidUpVotes = listOfUidUpVotes;
    this.requests = requests;
    this.requestUpVotes = requestUpVotes;
    this.name = name;
    this.partOf = partOf;
    this.selectedCategory = selectedCategory;
    this.selectedCreator = selectedCreator;
    this.thumbnail = thumbnail;
    this.thumbnailKey = thumbnailKey;
    this.playlist = playlist;
    this.createdDate = createdDate;
    this.updatedDate = updatedDate;
    this.ownerData = ownerData;
    this.owner = owner;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      RequestSong requestSong = (RequestSong) obj;
      return ObjectsCompat.equals(getKey(), requestSong.getKey()) &&
              ObjectsCompat.equals(getFileUrl(), requestSong.getFileUrl()) &&
              ObjectsCompat.equals(getFileKey(), requestSong.getFileKey()) &&
              ObjectsCompat.equals(getListens(), requestSong.getListens()) &&
              ObjectsCompat.equals(getTrendingListens(), requestSong.getTrendingListens()) &&
              ObjectsCompat.equals(getListOfUidDownVotes(), requestSong.getListOfUidDownVotes()) &&
              ObjectsCompat.equals(getListOfUidUpVotes(), requestSong.getListOfUidUpVotes()) &&
              ObjectsCompat.equals(getRequests(), requestSong.getRequests()) &&
              ObjectsCompat.equals(getRequestUpVotes(), requestSong.getRequestUpVotes()) &&
              ObjectsCompat.equals(getName(), requestSong.getName()) &&
              ObjectsCompat.equals(getPartOf(), requestSong.getPartOf()) &&
              ObjectsCompat.equals(getSelectedCategory(), requestSong.getSelectedCategory()) &&
              ObjectsCompat.equals(getSelectedCreator(), requestSong.getSelectedCreator()) &&
              ObjectsCompat.equals(getThumbnail(), requestSong.getThumbnail()) &&
              ObjectsCompat.equals(getThumbnailKey(), requestSong.getThumbnailKey()) &&
              ObjectsCompat.equals(getPlaylist(), requestSong.getPlaylist()) &&
              ObjectsCompat.equals(getCreatedDate(), requestSong.getCreatedDate()) &&
              ObjectsCompat.equals(getUpdatedDate(), requestSong.getUpdatedDate()) &&
              ObjectsCompat.equals(getOwnerData(), requestSong.getOwnerData()) &&
              ObjectsCompat.equals(getOwner(), requestSong.getOwner()) &&
              ObjectsCompat.equals(getCreatedAt(), requestSong.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), requestSong.getUpdatedAt());
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
      .append(getRequests())
      .append(getRequestUpVotes())
      .append(getName())
      .append(getPartOf())
      .append(getSelectedCategory())
      .append(getSelectedCreator())
      .append(getThumbnail())
      .append(getThumbnailKey())
      .append(getPlaylist())
      .append(getCreatedDate())
      .append(getUpdatedDate())
      .append(getOwnerData())
      .append(getOwner())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("RequestSong {")
      .append("key=" + String.valueOf(getKey()) + ", ")
      .append("fileUrl=" + String.valueOf(getFileUrl()) + ", ")
      .append("fileKey=" + String.valueOf(getFileKey()) + ", ")
      .append("listens=" + String.valueOf(getListens()) + ", ")
      .append("trendingListens=" + String.valueOf(getTrendingListens()) + ", ")
      .append("listOfUidDownVotes=" + String.valueOf(getListOfUidDownVotes()) + ", ")
      .append("listOfUidUpVotes=" + String.valueOf(getListOfUidUpVotes()) + ", ")
      .append("requests=" + String.valueOf(getRequests()) + ", ")
      .append("requestUpVotes=" + String.valueOf(getRequestUpVotes()) + ", ")
      .append("name=" + String.valueOf(getName()) + ", ")
      .append("partOf=" + String.valueOf(getPartOf()) + ", ")
      .append("selectedCategory=" + String.valueOf(getSelectedCategory()) + ", ")
      .append("selectedCreator=" + String.valueOf(getSelectedCreator()) + ", ")
      .append("thumbnail=" + String.valueOf(getThumbnail()) + ", ")
      .append("thumbnailKey=" + String.valueOf(getThumbnailKey()) + ", ")
      .append("playlist=" + String.valueOf(getPlaylist()) + ", ")
      .append("createdDate=" + String.valueOf(getCreatedDate()) + ", ")
      .append("updatedDate=" + String.valueOf(getUpdatedDate()) + ", ")
      .append("ownerData=" + String.valueOf(getOwnerData()) + ", ")
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
      requests,
      requestUpVotes,
      name,
      partOf,
      selectedCategory,
      selectedCreator,
      thumbnail,
      thumbnailKey,
      playlist,
      createdDate,
      updatedDate,
      ownerData,
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
    PlaylistStep thumbnailKey(String thumbnailKey);
  }
  

  public interface PlaylistStep {
    CreatedDateStep playlist(RequestPlaylist playlist);
  }
  

  public interface CreatedDateStep {
    UpdatedDateStep createdDate(Temporal.DateTime createdDate);
  }
  

  public interface UpdatedDateStep {
    BuildStep updatedDate(Temporal.DateTime updatedDate);
  }
  

  public interface BuildStep {
    RequestSong build();
    BuildStep listens(List<String> listens);
    BuildStep trendingListens(List<String> trendingListens);
    BuildStep listOfUidDownVotes(List<String> listOfUidDownVotes);
    BuildStep listOfUidUpVotes(List<String> listOfUidUpVotes);
    BuildStep requests(List<String> requests);
    BuildStep requestUpVotes(List<String> requestUpVotes);
    BuildStep partOf(String partOf);
    BuildStep selectedCreator(String selectedCreator);
    BuildStep ownerData(String ownerData);
    BuildStep owner(String owner);
  }
  

  public static class Builder implements KeyStep, FileUrlStep, FileKeyStep, NameStep, SelectedCategoryStep, ThumbnailStep, ThumbnailKeyStep, PlaylistStep, CreatedDateStep, UpdatedDateStep, BuildStep {
    private String key;
    private String fileUrl;
    private String fileKey;
    private String name;
    private String selectedCategory;
    private String thumbnail;
    private String thumbnailKey;
    private RequestPlaylist playlist;
    private Temporal.DateTime createdDate;
    private Temporal.DateTime updatedDate;
    private List<String> listens;
    private List<String> trendingListens;
    private List<String> listOfUidDownVotes;
    private List<String> listOfUidUpVotes;
    private List<String> requests;
    private List<String> requestUpVotes;
    private String partOf;
    private String selectedCreator;
    private String ownerData;
    private String owner;
    public Builder() {
      
    }
    
    private Builder(String key, String fileUrl, String fileKey, List<String> listens, List<String> trendingListens, List<String> listOfUidDownVotes, List<String> listOfUidUpVotes, List<String> requests, List<String> requestUpVotes, String name, String partOf, String selectedCategory, String selectedCreator, String thumbnail, String thumbnailKey, RequestPlaylist playlist, Temporal.DateTime createdDate, Temporal.DateTime updatedDate, String ownerData, String owner) {
      this.key = key;
      this.fileUrl = fileUrl;
      this.fileKey = fileKey;
      this.listens = listens;
      this.trendingListens = trendingListens;
      this.listOfUidDownVotes = listOfUidDownVotes;
      this.listOfUidUpVotes = listOfUidUpVotes;
      this.requests = requests;
      this.requestUpVotes = requestUpVotes;
      this.name = name;
      this.partOf = partOf;
      this.selectedCategory = selectedCategory;
      this.selectedCreator = selectedCreator;
      this.thumbnail = thumbnail;
      this.thumbnailKey = thumbnailKey;
      this.playlist = playlist;
      this.createdDate = createdDate;
      this.updatedDate = updatedDate;
      this.ownerData = ownerData;
      this.owner = owner;
    }
    
    @Override
     public RequestSong build() {
        
        return new RequestSong(
          key,
          fileUrl,
          fileKey,
          listens,
          trendingListens,
          listOfUidDownVotes,
          listOfUidUpVotes,
          requests,
          requestUpVotes,
          name,
          partOf,
          selectedCategory,
          selectedCreator,
          thumbnail,
          thumbnailKey,
          playlist,
          createdDate,
          updatedDate,
          ownerData,
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
     public PlaylistStep thumbnailKey(String thumbnailKey) {
        Objects.requireNonNull(thumbnailKey);
        this.thumbnailKey = thumbnailKey;
        return this;
    }
    
    @Override
     public CreatedDateStep playlist(RequestPlaylist playlist) {
        Objects.requireNonNull(playlist);
        this.playlist = playlist;
        return this;
    }
    
    @Override
     public UpdatedDateStep createdDate(Temporal.DateTime createdDate) {
        Objects.requireNonNull(createdDate);
        this.createdDate = createdDate;
        return this;
    }
    
    @Override
     public BuildStep updatedDate(Temporal.DateTime updatedDate) {
        Objects.requireNonNull(updatedDate);
        this.updatedDate = updatedDate;
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
     public BuildStep requests(List<String> requests) {
        this.requests = requests;
        return this;
    }
    
    @Override
     public BuildStep requestUpVotes(List<String> requestUpVotes) {
        this.requestUpVotes = requestUpVotes;
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
     public BuildStep ownerData(String ownerData) {
        this.ownerData = ownerData;
        return this;
    }
    
    @Override
     public BuildStep owner(String owner) {
        this.owner = owner;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String key, String fileUrl, String fileKey, List<String> listens, List<String> trendingListens, List<String> listOfUidDownVotes, List<String> listOfUidUpVotes, List<String> requests, List<String> requestUpVotes, String name, String partOf, String selectedCategory, String selectedCreator, String thumbnail, String thumbnailKey, RequestPlaylist playlist, Temporal.DateTime createdDate, Temporal.DateTime updatedDate, String ownerData, String owner) {
      super(key, fileUrl, fileKey, listens, trendingListens, listOfUidDownVotes, listOfUidUpVotes, requests, requestUpVotes, name, partOf, selectedCategory, selectedCreator, thumbnail, thumbnailKey, playlist, createdDate, updatedDate, ownerData, owner);
      Objects.requireNonNull(key);
      Objects.requireNonNull(fileUrl);
      Objects.requireNonNull(fileKey);
      Objects.requireNonNull(name);
      Objects.requireNonNull(selectedCategory);
      Objects.requireNonNull(thumbnail);
      Objects.requireNonNull(thumbnailKey);
      Objects.requireNonNull(playlist);
      Objects.requireNonNull(createdDate);
      Objects.requireNonNull(updatedDate);
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
     public CopyOfBuilder playlist(RequestPlaylist playlist) {
      return (CopyOfBuilder) super.playlist(playlist);
    }
    
    @Override
     public CopyOfBuilder createdDate(Temporal.DateTime createdDate) {
      return (CopyOfBuilder) super.createdDate(createdDate);
    }
    
    @Override
     public CopyOfBuilder updatedDate(Temporal.DateTime updatedDate) {
      return (CopyOfBuilder) super.updatedDate(updatedDate);
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
     public CopyOfBuilder requests(List<String> requests) {
      return (CopyOfBuilder) super.requests(requests);
    }
    
    @Override
     public CopyOfBuilder requestUpVotes(List<String> requestUpVotes) {
      return (CopyOfBuilder) super.requestUpVotes(requestUpVotes);
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
     public CopyOfBuilder ownerData(String ownerData) {
      return (CopyOfBuilder) super.ownerData(ownerData);
    }
    
    @Override
     public CopyOfBuilder owner(String owner) {
      return (CopyOfBuilder) super.owner(owner);
    }
  }
  

  public static class RequestSongIdentifier extends ModelIdentifier<RequestSong> {
    private static final long serialVersionUID = 1L;
    public RequestSongIdentifier(String key) {
      super(key);
    }
  }
  
}

package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.annotations.HasMany;
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

/** This is an auto generated class representing the RequestPlayer type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "RequestPlayers", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
  @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
})
@Index(name = "undefined", fields = {"key"})
public final class RequestPlayer implements Model {
  public static final QueryField KEY = field("RequestPlayer", "key");
  public static final QueryField NAME = field("RequestPlayer", "name");
  public static final QueryField DESC = field("RequestPlayer", "desc");
  public static final QueryField DEVICE = field("RequestPlayer", "device");
  public static final QueryField LONGITUDE = field("RequestPlayer", "longitude");
  public static final QueryField LATITUDE = field("RequestPlayer", "latitude");
  public static final QueryField FOLLOWERS = field("RequestPlayer", "followers");
  public static final QueryField FOLLOWING = field("RequestPlayer", "following");
  public static final QueryField CREATED_DATE = field("RequestPlayer", "createdDate");
  public static final QueryField UPDATED_DATE = field("RequestPlayer", "updatedDate");
  public static final QueryField OWNER_DATA = field("RequestPlayer", "ownerData");
  public static final QueryField OWNERS = field("RequestPlayer", "owners");
  private final @ModelField(targetType="ID", isRequired = true) String key;
  private final @ModelField(targetType="String") String name;
  private final @ModelField(targetType="String") String desc;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ, ModelOperation.UPDATE }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) String device;
  private final @ModelField(targetType="String") String longitude;
  private final @ModelField(targetType="String") String latitude;
  private final @ModelField(targetType="RequestPlaylist", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) @HasMany(associatedWith = "player", type = RequestPlaylist.class) List<RequestPlaylist> playlists = null;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE })
  }) List<String> followers;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE })
  }) List<String> following;
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
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE, ModelOperation.DELETE }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
  }) List<String> owners;
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
  
  public String getName() {
      return name;
  }
  
  public String getDesc() {
      return desc;
  }
  
  public String getDevice() {
      return device;
  }
  
  public String getLongitude() {
      return longitude;
  }
  
  public String getLatitude() {
      return latitude;
  }
  
  public List<RequestPlaylist> getPlaylists() {
      return playlists;
  }
  
  public List<String> getFollowers() {
      return followers;
  }
  
  public List<String> getFollowing() {
      return following;
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
  
  public List<String> getOwners() {
      return owners;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private RequestPlayer(String key, String name, String desc, String device, String longitude, String latitude, List<String> followers, List<String> following, Temporal.DateTime createdDate, Temporal.DateTime updatedDate, String ownerData, List<String> owners) {
    this.key = key;
    this.name = name;
    this.desc = desc;
    this.device = device;
    this.longitude = longitude;
    this.latitude = latitude;
    this.followers = followers;
    this.following = following;
    this.createdDate = createdDate;
    this.updatedDate = updatedDate;
    this.ownerData = ownerData;
    this.owners = owners;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      RequestPlayer requestPlayer = (RequestPlayer) obj;
      return ObjectsCompat.equals(getKey(), requestPlayer.getKey()) &&
              ObjectsCompat.equals(getName(), requestPlayer.getName()) &&
              ObjectsCompat.equals(getDesc(), requestPlayer.getDesc()) &&
              ObjectsCompat.equals(getDevice(), requestPlayer.getDevice()) &&
              ObjectsCompat.equals(getLongitude(), requestPlayer.getLongitude()) &&
              ObjectsCompat.equals(getLatitude(), requestPlayer.getLatitude()) &&
              ObjectsCompat.equals(getFollowers(), requestPlayer.getFollowers()) &&
              ObjectsCompat.equals(getFollowing(), requestPlayer.getFollowing()) &&
              ObjectsCompat.equals(getCreatedDate(), requestPlayer.getCreatedDate()) &&
              ObjectsCompat.equals(getUpdatedDate(), requestPlayer.getUpdatedDate()) &&
              ObjectsCompat.equals(getOwnerData(), requestPlayer.getOwnerData()) &&
              ObjectsCompat.equals(getOwners(), requestPlayer.getOwners()) &&
              ObjectsCompat.equals(getCreatedAt(), requestPlayer.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), requestPlayer.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getKey())
      .append(getName())
      .append(getDesc())
      .append(getDevice())
      .append(getLongitude())
      .append(getLatitude())
      .append(getFollowers())
      .append(getFollowing())
      .append(getCreatedDate())
      .append(getUpdatedDate())
      .append(getOwnerData())
      .append(getOwners())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("RequestPlayer {")
      .append("key=" + String.valueOf(getKey()) + ", ")
      .append("name=" + String.valueOf(getName()) + ", ")
      .append("desc=" + String.valueOf(getDesc()) + ", ")
      .append("device=" + String.valueOf(getDevice()) + ", ")
      .append("longitude=" + String.valueOf(getLongitude()) + ", ")
      .append("latitude=" + String.valueOf(getLatitude()) + ", ")
      .append("followers=" + String.valueOf(getFollowers()) + ", ")
      .append("following=" + String.valueOf(getFollowing()) + ", ")
      .append("createdDate=" + String.valueOf(getCreatedDate()) + ", ")
      .append("updatedDate=" + String.valueOf(getUpdatedDate()) + ", ")
      .append("ownerData=" + String.valueOf(getOwnerData()) + ", ")
      .append("owners=" + String.valueOf(getOwners()) + ", ")
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
      desc,
      device,
      longitude,
      latitude,
      followers,
      following,
      createdDate,
      updatedDate,
      ownerData,
      owners);
  }
  public interface KeyStep {
    CreatedDateStep key(String key);
  }
  

  public interface CreatedDateStep {
    UpdatedDateStep createdDate(Temporal.DateTime createdDate);
  }
  

  public interface UpdatedDateStep {
    BuildStep updatedDate(Temporal.DateTime updatedDate);
  }
  

  public interface BuildStep {
    RequestPlayer build();
    BuildStep name(String name);
    BuildStep desc(String desc);
    BuildStep device(String device);
    BuildStep longitude(String longitude);
    BuildStep latitude(String latitude);
    BuildStep followers(List<String> followers);
    BuildStep following(List<String> following);
    BuildStep ownerData(String ownerData);
    BuildStep owners(List<String> owners);
  }
  

  public static class Builder implements KeyStep, CreatedDateStep, UpdatedDateStep, BuildStep {
    private String key;
    private Temporal.DateTime createdDate;
    private Temporal.DateTime updatedDate;
    private String name;
    private String desc;
    private String device;
    private String longitude;
    private String latitude;
    private List<String> followers;
    private List<String> following;
    private String ownerData;
    private List<String> owners;
    public Builder() {
      
    }
    
    private Builder(String key, String name, String desc, String device, String longitude, String latitude, List<String> followers, List<String> following, Temporal.DateTime createdDate, Temporal.DateTime updatedDate, String ownerData, List<String> owners) {
      this.key = key;
      this.name = name;
      this.desc = desc;
      this.device = device;
      this.longitude = longitude;
      this.latitude = latitude;
      this.followers = followers;
      this.following = following;
      this.createdDate = createdDate;
      this.updatedDate = updatedDate;
      this.ownerData = ownerData;
      this.owners = owners;
    }
    
    @Override
     public RequestPlayer build() {
        
        return new RequestPlayer(
          key,
          name,
          desc,
          device,
          longitude,
          latitude,
          followers,
          following,
          createdDate,
          updatedDate,
          ownerData,
          owners);
    }
    
    @Override
     public CreatedDateStep key(String key) {
        Objects.requireNonNull(key);
        this.key = key;
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
     public BuildStep name(String name) {
        this.name = name;
        return this;
    }
    
    @Override
     public BuildStep desc(String desc) {
        this.desc = desc;
        return this;
    }
    
    @Override
     public BuildStep device(String device) {
        this.device = device;
        return this;
    }
    
    @Override
     public BuildStep longitude(String longitude) {
        this.longitude = longitude;
        return this;
    }
    
    @Override
     public BuildStep latitude(String latitude) {
        this.latitude = latitude;
        return this;
    }
    
    @Override
     public BuildStep followers(List<String> followers) {
        this.followers = followers;
        return this;
    }
    
    @Override
     public BuildStep following(List<String> following) {
        this.following = following;
        return this;
    }
    
    @Override
     public BuildStep ownerData(String ownerData) {
        this.ownerData = ownerData;
        return this;
    }
    
    @Override
     public BuildStep owners(List<String> owners) {
        this.owners = owners;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String key, String name, String desc, String device, String longitude, String latitude, List<String> followers, List<String> following, Temporal.DateTime createdDate, Temporal.DateTime updatedDate, String ownerData, List<String> owners) {
      super(key, name, desc, device, longitude, latitude, followers, following, createdDate, updatedDate, ownerData, owners);
      Objects.requireNonNull(key);
      Objects.requireNonNull(createdDate);
      Objects.requireNonNull(updatedDate);
    }
    
    @Override
     public CopyOfBuilder key(String key) {
      return (CopyOfBuilder) super.key(key);
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
     public CopyOfBuilder name(String name) {
      return (CopyOfBuilder) super.name(name);
    }
    
    @Override
     public CopyOfBuilder desc(String desc) {
      return (CopyOfBuilder) super.desc(desc);
    }
    
    @Override
     public CopyOfBuilder device(String device) {
      return (CopyOfBuilder) super.device(device);
    }
    
    @Override
     public CopyOfBuilder longitude(String longitude) {
      return (CopyOfBuilder) super.longitude(longitude);
    }
    
    @Override
     public CopyOfBuilder latitude(String latitude) {
      return (CopyOfBuilder) super.latitude(latitude);
    }
    
    @Override
     public CopyOfBuilder followers(List<String> followers) {
      return (CopyOfBuilder) super.followers(followers);
    }
    
    @Override
     public CopyOfBuilder following(List<String> following) {
      return (CopyOfBuilder) super.following(following);
    }
    
    @Override
     public CopyOfBuilder ownerData(String ownerData) {
      return (CopyOfBuilder) super.ownerData(ownerData);
    }
    
    @Override
     public CopyOfBuilder owners(List<String> owners) {
      return (CopyOfBuilder) super.owners(owners);
    }
  }
  

  public static class RequestPlayerIdentifier extends ModelIdentifier<RequestPlayer> {
    private static final long serialVersionUID = 1L;
    public RequestPlayerIdentifier(String key) {
      super(key);
    }
  }
  
}

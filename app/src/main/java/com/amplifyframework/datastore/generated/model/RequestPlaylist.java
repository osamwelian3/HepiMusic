package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.annotations.BelongsTo;
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

/** This is an auto generated class representing the RequestPlaylist type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "RequestPlaylists", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
  @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
})
@Index(name = "undefined", fields = {"key"})
public final class RequestPlaylist implements Model {
  public static final QueryField KEY = field("RequestPlaylist", "key");
  public static final QueryField NAME = field("RequestPlaylist", "name");
  public static final QueryField DESC = field("RequestPlaylist", "desc");
  public static final QueryField PLAYER = field("RequestPlaylist", "requestPlayerPlaylistsKey");
  public static final QueryField LISTENERS = field("RequestPlaylist", "listeners");
  public static final QueryField CREATED_DATE = field("RequestPlaylist", "createdDate");
  public static final QueryField UPDATED_DATE = field("RequestPlaylist", "updatedDate");
  public static final QueryField OWNER_DATA = field("RequestPlaylist", "ownerData");
  public static final QueryField OWNERS = field("RequestPlaylist", "owners");
  private final @ModelField(targetType="ID", isRequired = true) String key;
  private final @ModelField(targetType="String") String name;
  private final @ModelField(targetType="String") String desc;
  private final @ModelField(targetType="RequestPlayer", isRequired = true) @BelongsTo(targetName = "requestPlayerPlaylistsKey", targetNames = {"requestPlayerPlaylistsKey"}, type = RequestPlayer.class) RequestPlayer player;
  private final @ModelField(targetType="RequestSong", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE })
  }) @HasMany(associatedWith = "playlist", type = RequestSong.class) List<RequestSong> songs = null;
  private final @ModelField(targetType="String", authRules = {
    @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
    @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE })
  }) List<String> listeners;
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
  
  public RequestPlayer getPlayer() {
      return player;
  }
  
  public List<RequestSong> getSongs() {
      return songs;
  }
  
  public List<String> getListeners() {
      return listeners;
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
  
  private RequestPlaylist(String key, String name, String desc, RequestPlayer player, List<String> listeners, Temporal.DateTime createdDate, Temporal.DateTime updatedDate, String ownerData, List<String> owners) {
    this.key = key;
    this.name = name;
    this.desc = desc;
    this.player = player;
    this.listeners = listeners;
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
      RequestPlaylist requestPlaylist = (RequestPlaylist) obj;
      return ObjectsCompat.equals(getKey(), requestPlaylist.getKey()) &&
              ObjectsCompat.equals(getName(), requestPlaylist.getName()) &&
              ObjectsCompat.equals(getDesc(), requestPlaylist.getDesc()) &&
              ObjectsCompat.equals(getPlayer(), requestPlaylist.getPlayer()) &&
              ObjectsCompat.equals(getListeners(), requestPlaylist.getListeners()) &&
              ObjectsCompat.equals(getCreatedDate(), requestPlaylist.getCreatedDate()) &&
              ObjectsCompat.equals(getUpdatedDate(), requestPlaylist.getUpdatedDate()) &&
              ObjectsCompat.equals(getOwnerData(), requestPlaylist.getOwnerData()) &&
              ObjectsCompat.equals(getOwners(), requestPlaylist.getOwners()) &&
              ObjectsCompat.equals(getCreatedAt(), requestPlaylist.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), requestPlaylist.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getKey())
      .append(getName())
      .append(getDesc())
      .append(getPlayer())
      .append(getListeners())
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
      .append("RequestPlaylist {")
      .append("key=" + String.valueOf(getKey()) + ", ")
      .append("name=" + String.valueOf(getName()) + ", ")
      .append("desc=" + String.valueOf(getDesc()) + ", ")
      .append("player=" + String.valueOf(getPlayer()) + ", ")
      .append("listeners=" + String.valueOf(getListeners()) + ", ")
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
      player,
      listeners,
      createdDate,
      updatedDate,
      ownerData,
      owners);
  }
  public interface KeyStep {
    PlayerStep key(String key);
  }
  

  public interface PlayerStep {
    CreatedDateStep player(RequestPlayer player);
  }
  

  public interface CreatedDateStep {
    UpdatedDateStep createdDate(Temporal.DateTime createdDate);
  }
  

  public interface UpdatedDateStep {
    BuildStep updatedDate(Temporal.DateTime updatedDate);
  }
  

  public interface BuildStep {
    RequestPlaylist build();
    BuildStep name(String name);
    BuildStep desc(String desc);
    BuildStep listeners(List<String> listeners);
    BuildStep ownerData(String ownerData);
    BuildStep owners(List<String> owners);
  }
  

  public static class Builder implements KeyStep, PlayerStep, CreatedDateStep, UpdatedDateStep, BuildStep {
    private String key;
    private RequestPlayer player;
    private Temporal.DateTime createdDate;
    private Temporal.DateTime updatedDate;
    private String name;
    private String desc;
    private List<String> listeners;
    private String ownerData;
    private List<String> owners;
    public Builder() {
      
    }
    
    private Builder(String key, String name, String desc, RequestPlayer player, List<String> listeners, Temporal.DateTime createdDate, Temporal.DateTime updatedDate, String ownerData, List<String> owners) {
      this.key = key;
      this.name = name;
      this.desc = desc;
      this.player = player;
      this.listeners = listeners;
      this.createdDate = createdDate;
      this.updatedDate = updatedDate;
      this.ownerData = ownerData;
      this.owners = owners;
    }
    
    @Override
     public RequestPlaylist build() {
        
        return new RequestPlaylist(
          key,
          name,
          desc,
          player,
          listeners,
          createdDate,
          updatedDate,
          ownerData,
          owners);
    }
    
    @Override
     public PlayerStep key(String key) {
        Objects.requireNonNull(key);
        this.key = key;
        return this;
    }
    
    @Override
     public CreatedDateStep player(RequestPlayer player) {
        Objects.requireNonNull(player);
        this.player = player;
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
     public BuildStep listeners(List<String> listeners) {
        this.listeners = listeners;
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
    private CopyOfBuilder(String key, String name, String desc, RequestPlayer player, List<String> listeners, Temporal.DateTime createdDate, Temporal.DateTime updatedDate, String ownerData, List<String> owners) {
      super(key, name, desc, player, listeners, createdDate, updatedDate, ownerData, owners);
      Objects.requireNonNull(key);
      Objects.requireNonNull(player);
      Objects.requireNonNull(createdDate);
      Objects.requireNonNull(updatedDate);
    }
    
    @Override
     public CopyOfBuilder key(String key) {
      return (CopyOfBuilder) super.key(key);
    }
    
    @Override
     public CopyOfBuilder player(RequestPlayer player) {
      return (CopyOfBuilder) super.player(player);
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
     public CopyOfBuilder listeners(List<String> listeners) {
      return (CopyOfBuilder) super.listeners(listeners);
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
  

  public static class RequestPlaylistIdentifier extends ModelIdentifier<RequestPlaylist> {
    private static final long serialVersionUID = 1L;
    public RequestPlaylistIdentifier(String key) {
      super(key);
    }
  }
  
}

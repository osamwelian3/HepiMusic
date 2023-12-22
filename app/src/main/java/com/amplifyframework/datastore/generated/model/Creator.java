package com.amplifyframework.datastore.generated.model;

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

/** This is an auto generated class representing the Creator type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Creators", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
  @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
})
@Index(name = "undefined", fields = {"key"})
public final class Creator implements Model {
  public static final QueryField KEY = field("Creator", "key");
  public static final QueryField DESC = field("Creator", "desc");
  public static final QueryField FACEBOOK = field("Creator", "facebook");
  public static final QueryField INSTAGRAM = field("Creator", "instagram");
  public static final QueryField NAME = field("Creator", "name");
  public static final QueryField THUMBNAIL = field("Creator", "thumbnail");
  public static final QueryField THUMBNAIL_KEY = field("Creator", "thumbnailKey");
  public static final QueryField TWITTER = field("Creator", "twitter");
  public static final QueryField YOUTUBE = field("Creator", "youtube");
  public static final QueryField OWNER = field("Creator", "owner");
  private final @ModelField(targetType="ID", isRequired = true) String key;
  private final @ModelField(targetType="String") String desc;
  private final @ModelField(targetType="String") String facebook;
  private final @ModelField(targetType="String") String instagram;
  private final @ModelField(targetType="String", isRequired = true) String name;
  private final @ModelField(targetType="String") String thumbnail;
  private final @ModelField(targetType="String") String thumbnailKey;
  private final @ModelField(targetType="String") String twitter;
  private final @ModelField(targetType="String") String youtube;
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
  
  public String getDesc() {
      return desc;
  }
  
  public String getFacebook() {
      return facebook;
  }
  
  public String getInstagram() {
      return instagram;
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
  
  public String getTwitter() {
      return twitter;
  }
  
  public String getYoutube() {
      return youtube;
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
  
  private Creator(String key, String desc, String facebook, String instagram, String name, String thumbnail, String thumbnailKey, String twitter, String youtube, String owner) {
    this.key = key;
    this.desc = desc;
    this.facebook = facebook;
    this.instagram = instagram;
    this.name = name;
    this.thumbnail = thumbnail;
    this.thumbnailKey = thumbnailKey;
    this.twitter = twitter;
    this.youtube = youtube;
    this.owner = owner;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Creator creator = (Creator) obj;
      return ObjectsCompat.equals(getKey(), creator.getKey()) &&
              ObjectsCompat.equals(getDesc(), creator.getDesc()) &&
              ObjectsCompat.equals(getFacebook(), creator.getFacebook()) &&
              ObjectsCompat.equals(getInstagram(), creator.getInstagram()) &&
              ObjectsCompat.equals(getName(), creator.getName()) &&
              ObjectsCompat.equals(getThumbnail(), creator.getThumbnail()) &&
              ObjectsCompat.equals(getThumbnailKey(), creator.getThumbnailKey()) &&
              ObjectsCompat.equals(getTwitter(), creator.getTwitter()) &&
              ObjectsCompat.equals(getYoutube(), creator.getYoutube()) &&
              ObjectsCompat.equals(getOwner(), creator.getOwner()) &&
              ObjectsCompat.equals(getCreatedAt(), creator.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), creator.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getKey())
      .append(getDesc())
      .append(getFacebook())
      .append(getInstagram())
      .append(getName())
      .append(getThumbnail())
      .append(getThumbnailKey())
      .append(getTwitter())
      .append(getYoutube())
      .append(getOwner())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Creator {")
      .append("key=" + String.valueOf(getKey()) + ", ")
      .append("desc=" + String.valueOf(getDesc()) + ", ")
      .append("facebook=" + String.valueOf(getFacebook()) + ", ")
      .append("instagram=" + String.valueOf(getInstagram()) + ", ")
      .append("name=" + String.valueOf(getName()) + ", ")
      .append("thumbnail=" + String.valueOf(getThumbnail()) + ", ")
      .append("thumbnailKey=" + String.valueOf(getThumbnailKey()) + ", ")
      .append("twitter=" + String.valueOf(getTwitter()) + ", ")
      .append("youtube=" + String.valueOf(getYoutube()) + ", ")
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
      desc,
      facebook,
      instagram,
      name,
      thumbnail,
      thumbnailKey,
      twitter,
      youtube,
      owner);
  }
  public interface KeyStep {
    NameStep key(String key);
  }
  

  public interface NameStep {
    BuildStep name(String name);
  }
  

  public interface BuildStep {
    Creator build();
    BuildStep desc(String desc);
    BuildStep facebook(String facebook);
    BuildStep instagram(String instagram);
    BuildStep thumbnail(String thumbnail);
    BuildStep thumbnailKey(String thumbnailKey);
    BuildStep twitter(String twitter);
    BuildStep youtube(String youtube);
    BuildStep owner(String owner);
  }
  

  public static class Builder implements KeyStep, NameStep, BuildStep {
    private String key;
    private String name;
    private String desc;
    private String facebook;
    private String instagram;
    private String thumbnail;
    private String thumbnailKey;
    private String twitter;
    private String youtube;
    private String owner;
    public Builder() {
      
    }
    
    private Builder(String key, String desc, String facebook, String instagram, String name, String thumbnail, String thumbnailKey, String twitter, String youtube, String owner) {
      this.key = key;
      this.desc = desc;
      this.facebook = facebook;
      this.instagram = instagram;
      this.name = name;
      this.thumbnail = thumbnail;
      this.thumbnailKey = thumbnailKey;
      this.twitter = twitter;
      this.youtube = youtube;
      this.owner = owner;
    }
    
    @Override
     public Creator build() {
        
        return new Creator(
          key,
          desc,
          facebook,
          instagram,
          name,
          thumbnail,
          thumbnailKey,
          twitter,
          youtube,
          owner);
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
     public BuildStep desc(String desc) {
        this.desc = desc;
        return this;
    }
    
    @Override
     public BuildStep facebook(String facebook) {
        this.facebook = facebook;
        return this;
    }
    
    @Override
     public BuildStep instagram(String instagram) {
        this.instagram = instagram;
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
    
    @Override
     public BuildStep twitter(String twitter) {
        this.twitter = twitter;
        return this;
    }
    
    @Override
     public BuildStep youtube(String youtube) {
        this.youtube = youtube;
        return this;
    }
    
    @Override
     public BuildStep owner(String owner) {
        this.owner = owner;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String key, String desc, String facebook, String instagram, String name, String thumbnail, String thumbnailKey, String twitter, String youtube, String owner) {
      super(key, desc, facebook, instagram, name, thumbnail, thumbnailKey, twitter, youtube, owner);
      Objects.requireNonNull(key);
      Objects.requireNonNull(name);
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
     public CopyOfBuilder desc(String desc) {
      return (CopyOfBuilder) super.desc(desc);
    }
    
    @Override
     public CopyOfBuilder facebook(String facebook) {
      return (CopyOfBuilder) super.facebook(facebook);
    }
    
    @Override
     public CopyOfBuilder instagram(String instagram) {
      return (CopyOfBuilder) super.instagram(instagram);
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
     public CopyOfBuilder twitter(String twitter) {
      return (CopyOfBuilder) super.twitter(twitter);
    }
    
    @Override
     public CopyOfBuilder youtube(String youtube) {
      return (CopyOfBuilder) super.youtube(youtube);
    }
    
    @Override
     public CopyOfBuilder owner(String owner) {
      return (CopyOfBuilder) super.owner(owner);
    }
  }
  

  public static class CreatorIdentifier extends ModelIdentifier<Creator> {
    private static final long serialVersionUID = 1L;
    public CreatorIdentifier(String key) {
      super(key);
    }
  }
  
}

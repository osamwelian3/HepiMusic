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

/** This is an auto generated class representing the Category type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Categories", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owner", identityClaim = "sub::username", provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ }),
  @AuthRule(allow = AuthStrategy.PUBLIC, operations = { ModelOperation.READ }),
  @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = { ModelOperation.CREATE, ModelOperation.READ })
})
@Index(name = "undefined", fields = {"key"})
public final class Category implements Model {
  public static final QueryField KEY = field("Category", "key");
  public static final QueryField NAME = field("Category", "name");
  public static final QueryField OWNER = field("Category", "owner");
  private final @ModelField(targetType="ID", isRequired = true) String key;
  private final @ModelField(targetType="String", isRequired = true) String name;
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
  
  public String getName() {
      return name;
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
  
  private Category(String key, String name, String owner) {
    this.key = key;
    this.name = name;
    this.owner = owner;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Category category = (Category) obj;
      return ObjectsCompat.equals(getKey(), category.getKey()) &&
              ObjectsCompat.equals(getName(), category.getName()) &&
              ObjectsCompat.equals(getOwner(), category.getOwner()) &&
              ObjectsCompat.equals(getCreatedAt(), category.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), category.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getKey())
      .append(getName())
      .append(getOwner())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Category {")
      .append("key=" + String.valueOf(getKey()) + ", ")
      .append("name=" + String.valueOf(getName()) + ", ")
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
      name,
      owner);
  }
  public interface KeyStep {
    NameStep key(String key);
  }
  

  public interface NameStep {
    BuildStep name(String name);
  }
  

  public interface BuildStep {
    Category build();
    BuildStep owner(String owner);
  }
  

  public static class Builder implements KeyStep, NameStep, BuildStep {
    private String key;
    private String name;
    private String owner;
    public Builder() {
      
    }
    
    private Builder(String key, String name, String owner) {
      this.key = key;
      this.name = name;
      this.owner = owner;
    }
    
    @Override
     public Category build() {
        
        return new Category(
          key,
          name,
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
     public BuildStep owner(String owner) {
        this.owner = owner;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String key, String name, String owner) {
      super(key, name, owner);
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
     public CopyOfBuilder owner(String owner) {
      return (CopyOfBuilder) super.owner(owner);
    }
  }
  

  public static class CategoryIdentifier extends ModelIdentifier<Category> {
    private static final long serialVersionUID = 1L;
    public CategoryIdentifier(String key) {
      super(key);
    }
  }
  
}

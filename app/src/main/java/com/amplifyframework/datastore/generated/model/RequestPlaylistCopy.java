package com.amplifyframework.datastore.generated.model;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelIdentifier;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.BelongsTo;
import com.amplifyframework.core.model.annotations.HasMany;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;
import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.Objects;

/**
 * This is an auto generated class representing the RequestPlaylist type in your schema.
 */
@SuppressWarnings("all")
@ModelConfig(pluralName = "RequestPlaylists", type = Model.Type.USER, version = 1, authRules = {
        @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = {ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ}),
        @AuthRule(allow = AuthStrategy.PUBLIC, operations = {ModelOperation.READ}),
        @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = {ModelOperation.CREATE, ModelOperation.READ})
})
@Index(name = "undefined", fields = {"key"})
public final class RequestPlaylistCopy implements Model {
    public static final QueryField KEY = field("RequestPlaylist", "key");
    public static final QueryField NAME = field("RequestPlaylist", "name");
    public static final QueryField DESC = field("RequestPlaylist", "desc");
    public static final QueryField PLAYER = field("RequestPlaylist", "requestPlayerPlaylistsKey");
    public static final QueryField CREATED_DATE = field("RequestPlayer", "createdDate");
    public static final QueryField UPDATED_DATE = field("RequestPlayer", "updatedDate");
    public static final QueryField OWNER_DATA = field("RequestPlayer", "ownerData");
    public static final QueryField OWNERS = field("RequestPlaylist", "owners");
    private final @ModelField(targetType = "ID", isRequired = true) String key;
    private final @ModelField(targetType = "String") String name;
    private final @ModelField(targetType = "String") String desc;
    private final @ModelField(targetType = "RequestPlayer", isRequired = true)
    @BelongsTo(targetName = "requestPlayerPlaylistsKey", targetNames = {"requestPlayerPlaylistsKey"}, type = RequestPlayer.class) RequestPlayer player;
    private final @ModelField(targetType = "RequestSong", authRules = {
            @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = {ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ}),
            @AuthRule(allow = AuthStrategy.PUBLIC, operations = {ModelOperation.READ}),
            @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = {ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE})
    })
    @HasMany(associatedWith = "playlist", type = RequestSong.class) List<RequestSong> songs;
    private final @ModelField(targetType = "RequestPlaylistCopy", authRules = {
            @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = {ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ}),
            @AuthRule(allow = AuthStrategy.PUBLIC, operations = {ModelOperation.READ}),
            @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = {ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE})
    })
    @HasMany(associatedWith = "playlist", type = RequestPlaylist.class) RequestPlaylist requestPlaylist;
    private final @ModelField(targetType = "Profile", authRules = {
            @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = {ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ}),
            @AuthRule(allow = AuthStrategy.PUBLIC, operations = {ModelOperation.READ}),
            @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = {ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE})
    })
    @HasMany(associatedWith = "requestPlaylistListenersKey", type = Profile.class) List<Profile> listeners = null;
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
    private final @ModelField(targetType = "String", authRules = {
            @AuthRule(allow = AuthStrategy.OWNER, ownerField = "owners", identityClaim = "sub::username", provider = "userPools", operations = {ModelOperation.CREATE, ModelOperation.READ, ModelOperation.UPDATE, ModelOperation.DELETE}),
            @AuthRule(allow = AuthStrategy.PUBLIC, operations = {ModelOperation.READ}),
            @AuthRule(allow = AuthStrategy.PRIVATE, provider = "userPools", operations = {ModelOperation.CREATE, ModelOperation.READ})
    }) List<String> owners;
    private @ModelField(targetType = "AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
    private @ModelField(targetType = "AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;

    /**
     * @deprecated This API is internal to Amplify and should not be used.
     */
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

    public RequestPlaylist getPlaylist() {
        return requestPlaylist;
    }

    public List<Profile> getListeners() {
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

    private RequestPlaylistCopy(String key, String name, String desc, RequestPlayer player, RequestPlaylist requestPlaylist, List<RequestSong> songs, Temporal.DateTime createdDate, Temporal.DateTime updatedDate, String ownerData, List<String> owners, Temporal.DateTime createdAt, Temporal.DateTime updatedAt) {
        this.key = key;
        this.name = name;
        this.desc = desc;
        this.player = player;
        this.requestPlaylist = requestPlaylist;
        this.songs = songs;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.ownerData = ownerData;
        this.owners = owners;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null || getClass() != obj.getClass()) {
            return false;
        } else {
            RequestPlaylistCopy requestPlaylist = (RequestPlaylistCopy) obj;
            return ObjectsCompat.equals(getKey(), requestPlaylist.getKey()) &&
                    ObjectsCompat.equals(getName(), requestPlaylist.getName()) &&
                    ObjectsCompat.equals(getDesc(), requestPlaylist.getDesc()) &&
                    ObjectsCompat.equals(getPlayer(), requestPlaylist.getPlayer()) &&
                    ObjectsCompat.equals(getPlaylist(), requestPlaylist.getPlaylist()) &&
                    ObjectsCompat.equals(getSongs(), requestPlaylist.getSongs()) &&
                    ObjectsCompat.equals(getCreatedDate(), getCreatedDate()) &&
                    ObjectsCompat.equals(getUpdatedDate(), getUpdatedDate()) &&
                    ObjectsCompat.equals(getOwnerData(), getOwnerData()) &&
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
                .append(getPlaylist())
                .append(getSongs())
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
                .append("requestPlaylist=" + String.valueOf(getPlaylist()) + ", ")
                .append("songs=" + String.valueOf(getSongs()) + ", ")
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
                requestPlaylist,
                songs,
                createdDate,
                updatedDate,
                ownerData,
                owners,
                createdAt,
                updatedAt);
    }

    public interface KeyStep {
        PlayerStep key(String key);
    }


    public interface PlayerStep {
        BuildStep player(RequestPlayer player);
    }


    public interface BuildStep {
        RequestPlaylistCopy build();

        BuildStep name(String name);

        BuildStep desc(String desc);

        BuildStep requestPlaylist(RequestPlaylist requestPlaylist);

        BuildStep songs(List<RequestSong> songs);

        BuildStep createdDate(Temporal.DateTime createdDate);

        BuildStep updatedDate(Temporal.DateTime updatedDate);

        BuildStep ownerData(String ownerData);

        BuildStep owners(List<String> owners);

        BuildStep createdAt(Temporal.DateTime createdAt);

        BuildStep updatedAt(Temporal.DateTime updatedAt);
    }


    public static class Builder implements KeyStep, PlayerStep, BuildStep {
        private String key;
        private RequestPlayer player;
        private String name;
        private String desc;
        private RequestPlaylist requestPlaylist;
        private List<RequestSong> songs;
        private Temporal.DateTime createdDate;
        private Temporal.DateTime updatedDate;
        private String ownerData;
        private List<String> owners;
        private Temporal.DateTime createdAt;
        private Temporal.DateTime updatedAt;

        public Builder() {

        }

        private Builder(String key, String name, String desc, RequestPlayer player, RequestPlaylist requestPlaylist, List<RequestSong> songs, Temporal.DateTime createdDate, Temporal.DateTime updatedDate, String ownerData, List<String> owners, Temporal.DateTime createdAt, Temporal.DateTime updatedAt) {
            this.key = key;
            this.name = name;
            this.desc = desc;
            this.player = player;
            this.requestPlaylist = requestPlaylist;
            this.songs = songs;
            this.createdDate = createdDate;
            this.updatedDate = updatedDate;
            this.ownerData = ownerData;
            this.owners = owners;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        @Override
        public RequestPlaylistCopy build() {

            return new RequestPlaylistCopy(
                    key,
                    name,
                    desc,
                    player,
                    requestPlaylist,
                    songs,
                    createdDate,
                    updatedDate,
                    ownerData,
                    owners,
                    createdAt,
                    updatedAt);
        }

        @Override
        public PlayerStep key(String key) {
            Objects.requireNonNull(key);
            this.key = key;
            return this;
        }

        @Override
        public BuildStep player(RequestPlayer player) {
            Objects.requireNonNull(player);
            this.player = player;
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
        public BuildStep requestPlaylist(RequestPlaylist requestPlaylist) {
            this.requestPlaylist = requestPlaylist;
            return this;
        }

        @Override
        public BuildStep songs(List<RequestSong> songs) {
            this.songs = songs;
            return this;
        }

        @Override
        public BuildStep createdDate(Temporal.DateTime createdDate) {
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
        public BuildStep ownerData(String ownerData) {
            this.ownerData = ownerData;
            return this;
        }

        @Override
        public BuildStep owners(List<String> owners) {
            this.owners = owners;
            return this;
        }

        @Override
        public BuildStep createdAt(Temporal.DateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Override
        public BuildStep updatedAt(Temporal.DateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
    }


    public final class CopyOfBuilder extends Builder {
        private CopyOfBuilder(String key, String name, String desc, RequestPlayer player, RequestPlaylist requestPlaylist, List<RequestSong> songs, Temporal.DateTime createdDate, Temporal.DateTime updatedDate, String ownerData, List<String> owners, Temporal.DateTime createdAt, Temporal.DateTime updatedAt) {
            super(key, name, desc, player, requestPlaylist, songs, createdDate, updatedDate, ownerData, owners, createdAt, updatedAt);
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
        public CopyOfBuilder name(String name) {
            return (CopyOfBuilder) super.name(name);
        }

        @Override
        public CopyOfBuilder desc(String desc) {
            return (CopyOfBuilder) super.desc(desc);
        }

        @Override
        public CopyOfBuilder requestPlaylist(RequestPlaylist requestPlaylist) {
            return (CopyOfBuilder) super.requestPlaylist(requestPlaylist);
        }

        @Override
        public CopyOfBuilder songs(List<RequestSong> songs) {
            return (CopyOfBuilder) super.songs(songs);
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
        public CopyOfBuilder ownerData(String ownerData) {
            return (CopyOfBuilder) super.ownerData(ownerData);
        }

        @Override
        public CopyOfBuilder owners(List<String> owners) {
            return (CopyOfBuilder) super.owners(owners);
        }

        @Override
        public CopyOfBuilder createdAt(Temporal.DateTime createdAt) {
            return (CopyOfBuilder) super.createdAt(createdAt);
        }

        @Override
        public CopyOfBuilder updatedAt(Temporal.DateTime updatedAt) {
            return (CopyOfBuilder) super.updatedAt(updatedAt);
        }
    }


    public static class RequestPlaylistIdentifier extends ModelIdentifier<RequestPlaylistCopy> {
        private static final long serialVersionUID = 1L;

        public RequestPlaylistIdentifier(String key) {
            super(key);
        }
    }

}

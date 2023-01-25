package me.marvin.proxy.networking.packet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.marvin.proxy.networking.ProtocolDirection;
import me.marvin.proxy.networking.ProtocolPhase;
import me.marvin.proxy.networking.Version;
import me.marvin.proxy.utils.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Packet types.
 */
public interface PacketTypes {
    /**
     * Loads all packet types.
     */
    static void load() {
        Handshake.load();
        Status.load();
        Login.load();
        Play.load();
    }

    /**
     * Tries to find the packet type according to the given version and id.
     *
     * @param direction the direction
     * @param phase the phase
     * @param id the id
     * @param version the version
     * @return the found packet type
     * @throws NullPointerException if no packet type was found
     */
    @NotNull
    static PacketType findThrowing(@NotNull ProtocolDirection direction, @NotNull ProtocolPhase phase, int id, @NotNull Version version) {
        return Objects.requireNonNull(find(direction, phase, id, version), "can't find packet (%s, %s, id: %d, %s)".formatted(direction, phase, id, version));
    }

    /**
     * Tries to find the packet type according to the given version and id.
     *
     * @param direction the direction
     * @param phase the phase
     * @param id the id
     * @param version the version
     * @return the found packet type or null
     */
    @Nullable
    static PacketType find(@NotNull ProtocolDirection direction, @NotNull ProtocolPhase phase, int id, @NotNull Version version) {
        Map<Version, PacketType[]> packetMap = Cache.VERSIONS.get(phase).get(direction);
        PacketType[] types = packetMap.get(version);

        if (types != null) {
            return types.length > id ? types[id] : null;
        }

        Version closest = Cache.CLOSEST_VERSION_CACHE.computeIfAbsent(version, __ -> Cache.findClosest(version));
        types = packetMap.get(closest);

        return types.length > id ? types[id] : null;
    }

    /**
     * Mutable packet type.
     */
    sealed interface MutablePacketType extends PacketType permits Handshake, Status, Login, Play {
        /**
         * {@inheritDoc}
         *
         * @param version the version
         * @return the id of this packet, or -1 if it doesn't exist for the given version
         */
        @Override
        default int id(@NotNull Version version) {
            return PacketType.super.id(Cache.CLOSEST_VERSION_CACHE.computeIfAbsent(version, __ -> Cache.findClosest(version)).version());
        }

        /**
         * {@inheritDoc}
         *
         * @param version the version
         * @return the id of this packet, or -1 if it doesn't exist for the given version
         */
        @Override
        default int id(int version) {
            return PacketType.super.id(Cache.findClosest(Version.exact(version)).version());
        }
    }

    /**
     * Handshake packets.
     */
    sealed interface Handshake extends MutablePacketType {
        static void load() {
            Cache.loadMappings(Client.class, Cache.mapping("handshake_client.json"), p -> p.versionMap);
        }

        @Override
        @NotNull
        default ProtocolPhase phase() {
            return ProtocolPhase.HANDSHAKE;
        }

        enum Client implements Handshake {
            SET_PROTOCOL;

            private final Map<Integer, Integer> versionMap = new HashMap<>();

            @Override
            @NotNull
            public Map<Integer, Integer> versionMap() {
                return Collections.unmodifiableMap(versionMap);
            }

            @Override
            @NotNull
            public ProtocolDirection direction() {
                return ProtocolDirection.CLIENT;
            }
        }
    }

    /**
     * Play packets.
     */
    sealed interface Play extends MutablePacketType {
        static void load() {
            Cache.loadMappings(Client.class, Cache.mapping("play_client.json"), p -> p.versionMap);
            Cache.loadMappings(Server.class, Cache.mapping("play_server.json"), p -> p.versionMap);
        }

        @Override
        @NotNull
        default ProtocolPhase phase() {
            return ProtocolPhase.PLAY;
        }

        enum Client implements Play {
            TELEPORT_CONFIRM, QUERY_BLOCK_NBT, SET_DIFFICULTY, CHAT_MESSAGE, CLIENT_STATUS, CLIENT_SETTINGS,
            TAB_COMPLETE, WINDOW_CONFIRMATION, CLICK_WINDOW_BUTTON, CLICK_WINDOW, CLOSE_WINDOW, PLUGIN_MESSAGE, EDIT_BOOK,
            QUERY_ENTITY_NBT, INTERACT_ENTITY, GENERATE_STRUCTURE, KEEP_ALIVE, LOCK_DIFFICULTY, PLAYER_POSITION, PLAYER_POSITION_AND_ROTATION,
            PLAYER_ROTATION, PLAYER_FLYING, VEHICLE_MOVE, STEER_BOAT, PICK_ITEM, CRAFT_RECIPE_REQUEST, PLAYER_ABILITIES, PLAYER_DIGGING,
            ENTITY_ACTION, STEER_VEHICLE, PONG, RECIPE_BOOK_DATA, SET_DISPLAYED_RECIPE, SET_RECIPE_BOOK_STATE, NAME_ITEM, RESOURCE_PACK_STATUS,
            ADVANCEMENT_TAB, SELECT_TRADE, SET_BEACON_EFFECT, HELD_ITEM_CHANGE, UPDATE_COMMAND_BLOCK, UPDATE_COMMAND_BLOCK_MINECART,
            CREATIVE_INVENTORY_ACTION, UPDATE_JIGSAW_BLOCK, UPDATE_STRUCTURE_BLOCK, UPDATE_SIGN, ANIMATION, SPECTATE, PLAYER_BLOCK_PLACEMENT,
            USE_ITEM;

            private final Map<Integer, Integer> versionMap = new HashMap<>();

            @Override
            @NotNull
            public Map<Integer, Integer> versionMap() {
                return Collections.unmodifiableMap(versionMap);
            }

            @Override
            @NotNull
            public ProtocolDirection direction() {
                return ProtocolDirection.CLIENT;
            }
        }

        enum Server implements Play {
            SET_COMPRESSION, MAP_CHUNK_BULK, UPDATE_ENTITY_NBT, UPDATE_SIGN, USE_BED, SPAWN_WEATHER_ENTITY,
            TITLE, WORLD_BORDER, COMBAT_EVENT, ENTITY_MOVEMENT, WINDOW_CONFIRMATION, SPAWN_ENTITY, SPAWN_EXPERIENCE_ORB, SPAWN_LIVING_ENTITY,
            SPAWN_PAINTING, SPAWN_PLAYER, SCULK_VIBRATION_SIGNAL, ENTITY_ANIMATION, STATISTICS, ACKNOWLEDGE_PLAYER_DIGGING, BLOCK_BREAK_ANIMATION,
            BLOCK_ENTITY_DATA, BLOCK_ACTION, BLOCK_CHANGE, BOSS_BAR, SERVER_DIFFICULTY, CHAT_MESSAGE, CLEAR_TITLES, TAB_COMPLETE, MULTI_BLOCK_CHANGE,
            DECLARE_COMMANDS, CLOSE_WINDOW, WINDOW_ITEMS, WINDOW_PROPERTY, SET_SLOT, SET_COOLDOWN, PLUGIN_MESSAGE, NAMED_SOUND_EFFECT, DISCONNECT,
            ENTITY_STATUS, EXPLOSION, UNLOAD_CHUNK, CHANGE_GAME_STATE, OPEN_HORSE_WINDOW, INITIALIZE_WORLD_BORDER, KEEP_ALIVE, CHUNK_DATA,
            EFFECT, PARTICLE, UPDATE_LIGHT, JOIN_GAME, MAP_DATA, TRADE_LIST, ENTITY_RELATIVE_MOVE, ENTITY_RELATIVE_MOVE_AND_ROTATION, ENTITY_ROTATION,
            VEHICLE_MOVE, OPEN_BOOK, OPEN_WINDOW, OPEN_SIGN_EDITOR, PING, CRAFT_RECIPE_RESPONSE, PLAYER_ABILITIES, END_COMBAT_EVENT, ENTER_COMBAT_EVENT,
            DEATH_COMBAT_EVENT, PLAYER_INFO, FACE_PLAYER, PLAYER_POSITION_AND_LOOK, UNLOCK_RECIPES, DESTROY_ENTITIES, REMOVE_ENTITY_EFFECT, RESOURCE_PACK_SEND,
            RESPAWN, ENTITY_HEAD_LOOK, SELECT_ADVANCEMENT_TAB, ACTION_BAR, WORLD_BORDER_CENTER, WORLD_BORDER_LERP_SIZE, WORLD_BORDER_SIZE, WORLD_BORDER_WARNING_DELAY,
            WORLD_BORDER_WARNING_REACH, CAMERA, HELD_ITEM_CHANGE, UPDATE_VIEW_POSITION, UPDATE_VIEW_DISTANCE, SPAWN_POSITION, DISPLAY_SCOREBOARD,
            ENTITY_METADATA, ATTACH_ENTITY, ENTITY_VELOCITY, ENTITY_EQUIPMENT, SET_EXPERIENCE, UPDATE_HEALTH, SCOREBOARD_OBJECTIVE, SET_PASSENGERS,
            TEAMS, UPDATE_SCORE, UPDATE_SIMULATION_DISTANCE, SET_TITLE_SUBTITLE, TIME_UPDATE, SET_TITLE_TEXT, SET_TITLE_TIMES, ENTITY_SOUND_EFFECT,
            SOUND_EFFECT, STOP_SOUND, PLAYER_LIST_HEADER_AND_FOOTER, NBT_QUERY_RESPONSE, COLLECT_ITEM, ENTITY_TELEPORT, ADVANCEMENTS, ENTITY_PROPERTIES,
            ENTITY_EFFECT, DECLARE_RECIPES, TAGS;

            private final Map<Integer, Integer> versionMap = new HashMap<>();

            @Override
            @NotNull
            public Map<Integer, Integer> versionMap() {
                return Collections.unmodifiableMap(versionMap);
            }

            @Override
            @NotNull
            public ProtocolDirection direction() {
                return ProtocolDirection.SERVER;
            }
        }
    }

    /**
     * Status packets.
     */
    sealed interface Status extends MutablePacketType {
        static void load() {
            Cache.loadMappings(Client.class, Cache.mapping("status_client.json"), p -> p.versionMap);
            Cache.loadMappings(Server.class, Cache.mapping("status_server.json"), p -> p.versionMap);
        }

        @Override
        @NotNull
        default ProtocolPhase phase() {
            return ProtocolPhase.STATUS;
        }

        enum Client implements Status {
            START, PING;

            private final Map<Integer, Integer> versionMap = new HashMap<>();

            @Override
            @NotNull
            public Map<Integer, Integer> versionMap() {
                return Collections.unmodifiableMap(versionMap);
            }

            @Override
            @NotNull
            public ProtocolDirection direction() {
                return ProtocolDirection.CLIENT;
            }
        }

        enum Server implements Status {
            SERVER_INFO, PONG;

            private final Map<Integer, Integer> versionMap = new HashMap<>();

            @Override
            @NotNull
            public Map<Integer, Integer> versionMap() {
                return Collections.unmodifiableMap(versionMap);
            }

            @Override
            @NotNull
            public ProtocolDirection direction() {
                return ProtocolDirection.SERVER;
            }
        }
    }

    /**
     * Login packets.
     */
    sealed interface Login extends MutablePacketType {
        static void load() {
            Cache.loadMappings(Client.class, Cache.mapping("login_client.json"), p -> p.versionMap);
            Cache.loadMappings(Server.class, Cache.mapping("login_server.json"), p -> p.versionMap);
        }

        @Override
        @NotNull
        default ProtocolPhase phase() {
            return ProtocolPhase.LOGIN;
        }

        enum Client implements Login {
            LOGIN_START, ENCRYPTION_RESPONSE, LOGIN_PLUGIN_RESPONSE;

            private final Map<Integer, Integer> versionMap = new HashMap<>();

            @Override
            @NotNull
            public Map<Integer, Integer> versionMap() {
                return Collections.unmodifiableMap(versionMap);
            }

            @Override
            @NotNull
            public ProtocolDirection direction() {
                return ProtocolDirection.CLIENT;
            }
        }

        enum Server implements Login {
            DISCONNECT, ENCRYPTION_REQUEST, LOGIN_SUCCESS, SET_COMPRESSION, LOGIN_PLUGIN_REQUEST;

            private final Map<Integer, Integer> versionMap = new HashMap<>();

            @Override
            @NotNull
            public Map<Integer, Integer> versionMap() {
                return Collections.unmodifiableMap(versionMap);
            }

            @Override
            @NotNull
            public ProtocolDirection direction() {
                return ProtocolDirection.SERVER;
            }
        }
    }

    final class Cache {
        private static final Map<ProtocolPhase, Map<ProtocolDirection, Map<Version, PacketType[]>>> VERSIONS = new EnumMap<>(ProtocolPhase.class);
        private static final Map<Version, Version> CLOSEST_VERSION_CACHE = new HashMap<>();

        private static final List<Tuple<Version, String>> MAIN_PROTOCOL_VERSIONS = Stream.of(
            Tuple.tuple(Version.V1_7_2, "V1_7_2"),
            Tuple.tuple(Version.V1_8, "V1_8"),
            Tuple.tuple(Version.V1_9, "V1_9"),
            Tuple.tuple(Version.V1_12, "V1_12"),
            Tuple.tuple(Version.V1_12_1, "V1_12_1"),
            Tuple.tuple(Version.V1_13, "V1_13"),
            Tuple.tuple(Version.V1_14, "V1_14"),
            Tuple.tuple(Version.V1_15_2, "V1_15_2"),
            Tuple.tuple(Version.V1_16, "V1_16"),
            Tuple.tuple(Version.V1_16_2, "V1_16_2"),
            Tuple.tuple(Version.V1_17, "V1_17"),
            Tuple.tuple(Version.V1_18, "V1_18")
        ).toList();

        private static <T extends Enum<?> & MutablePacketType> void loadMappings(Class<T> clazz, JsonObject mapping, Function<T, Map<Integer, Integer>> registry) {
            T[] rawConstants = clazz.getEnumConstants();
            Map<String, T> constants = Arrays.stream(rawConstants)
                .collect(Collectors.toMap(e -> e.name(), Function.identity()));

            for (Tuple<Version, String> tuple : MAIN_PROTOCOL_VERSIONS) {
                Version version = tuple.first();
                if (mapping.has(tuple.second())) {
                    JsonArray array = mapping.getAsJsonArray(tuple.second());

                    for (int i = 0; i < array.size(); i++) {
                        T packet = Objects.requireNonNull(constants.get(array.get(i).getAsString()), "can't add id because packet is null (" + clazz.getName() + ", " + array +  ", " + array.get(i).getAsString() + ", " + version.toString() + ")");
                        registry.apply(packet)
                            .put(version.version(), i);
                    }

                    registerVersion(version, rawConstants);
                }
            }
        }

        private static void registerVersion(Version version, PacketType[] types) {
            for (PacketType type : types) {
                PacketType[] perVerSection = VERSIONS
                    .computeIfAbsent(type.phase(), __ -> new EnumMap<>(ProtocolDirection.class))
                    .computeIfAbsent(type.direction(), __ -> new HashMap<>())
                    .computeIfAbsent(version, __ -> new PacketType[types.length]);
                int id = type.id(version);
                if (id != -1) {
                    perVerSection[id] = type;
                }
            }
        }

        private static Version findClosest(Version version) {
            return MAIN_PROTOCOL_VERSIONS.stream()
                .map(Tuple::first)
                .filter(Objects::nonNull)
                .sorted(Comparator.<Version>comparingInt(Version::version).reversed())
                .filter(v -> v.isOlderOr(version))
                .findFirst()
                .orElse(MAIN_PROTOCOL_VERSIONS.get(MAIN_PROTOCOL_VERSIONS.size() - 1).first());
        }

        private static JsonObject mapping(String path) {
            path = "packet/" + path;
            InputStream stream = Objects.requireNonNull(PacketTypes.class.getClassLoader().getResourceAsStream(path), "can't find resource '" + path + "'");

            try (Reader reader = new InputStreamReader(stream)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }
}

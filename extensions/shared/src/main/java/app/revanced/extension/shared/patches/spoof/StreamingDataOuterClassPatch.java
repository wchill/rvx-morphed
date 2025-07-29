package app.revanced.extension.shared.patches.spoof;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.protos.youtube.api.innertube.StreamingDataOuterClass.StreamingData;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.List;

import app.revanced.extension.shared.utils.Logger;

@SuppressWarnings("unused")
public class StreamingDataOuterClassPatch {
    public interface StreamingDataMessage {
        // Methods are added to YT classes during patching.
        StreamingData parseFrom(ByteBuffer responseProto);
    }

    // com.google.protos.youtube.api.innertube.StreamingDataOuterClass.StreamingData
    // It is based on YouTube 19.47.53, but 'field c' to 'field k' are the same regardless of the YouTube version.
    private interface StreamingDataFields {
        // int
        String unknownField_c = "c";

        // long
        String expiresInSeconds = "d";

        // List<?>
        String formats = "e";

        // List<?>
        String adaptiveFormats = "f";

        // List<?>
        String metadataFormats = "g";

        // String
        String dashManifestUrl = "h";

        // String
        String hlsManifestUrl = "i";

        // String
        String unknownField_j = "j";

        // String
        String drmParams = "k";

        // String
        String serverAbrStreamingUrl = "l";

        // List<?>
        // licenseInfos? or initialAuthorizedDrmTrackTypes?
        String unknownField_m = "m";
    }

    // It is based on YouTube 19.47.53, but all fields are the same regardless of the YouTube version.
    private interface FormatFields {
        // Double
        String unknownField_A = "A";

        // Double
        String unknownField_B = "B";

        // int
        String unknownField_C = "C";

        // Message
        String unknownField_D = "D";

        // int
        String unknownField_E = "E";

        // long
        String approxDurationMs = "F";

        // long
        String audioSampleRate = "G";

        // int
        String audioChannels = "H";

        // Float
        String loudnessDb = "I";

        // Float
        String unknownField_J = "J";

        // String
        String isDrc = "K";

        // Byte
        String unknownField_M = "M";

        // int
        String unknownField_c = "c";

        // int
        String unknownField_d = "d";

        // int
        String itag = "e";

        // String
        String url = "f";

        // String
        String mimeType = "g";

        // int
        String bitrate = "h";

        // int
        String averageBitrate = "i";

        // int
        String width = "j";

        // int
        String height = "k";

        // int
        String fps = "m";

        // Message
        String unknownField_n = "n";

        // Message
        String unknownField_o = "o";

        // long
        String lastModified = "p";

        // long
        String contentLength = "q";

        // String
        String xtags = "r";

        // Message
        String drmFamilies = "s";

        // String
        String qualityLabel = "t";

        // int
        String unknownField_u = "u";

        // int
        String unknownField_v = "v";

        // int
        String unknownField_w = "w";

        // Message
        String audioTrack = "x";

        // Message
        String unknownField_y = "y";

        // Message
        String unknownField_z = "z";
    }

    private static WeakReference<StreamingDataMessage> streamingDataMessageRef = new WeakReference<>(null);

    public static void initialize(@NonNull StreamingDataMessage streamingDataMessage) {
        streamingDataMessageRef = new WeakReference<>(streamingDataMessage);
    }

    /**
     * Parse the Proto Buffer and convert it to StreamingData (GeneratedMessage).
     * @param responseProto Proto Buffer.
     * @return              StreamingData (GeneratedMessage) parsed by ProtoParser.
     */
    @Nullable
    public static StreamingData parseFrom(ByteBuffer responseProto) {
        try {
            StreamingDataMessage streamingDataMessage = streamingDataMessageRef.get();
            if (streamingDataMessage == null) {
                Logger.printDebug(() -> "Cannot parseFrom because streaming data is null");
            } else {
                return streamingDataMessage.parseFrom(responseProto);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "parseFrom failure", ex);
        }
        return null;
    }

    /**
     * Get adaptiveFormats from parsed streamingData.
     * <p>
     * @param streamingData StreamingData (GeneratedMessage) parsed by ProtoParser.
     * @return              AdaptiveFormats (ProtoList).
     */
    public static List<?> getAdaptiveFormats(StreamingData streamingData) {
        try {
            if (streamingData != null) {
                Field field = streamingData.getClass().getField(StreamingDataFields.adaptiveFormats);
                field.setAccessible(true);
                if (field.get(streamingData) instanceof List<?> adaptiveFormats) {
                    return adaptiveFormats;
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "getAdaptiveFormats failed", ex);
        }
        return null;
    }

    /**
     * Set the deobfuscated streaming url in the 'url' field of adaptiveFormat.
     * <p>
     * @param adaptiveFormat AdaptiveFormat (GeneratedMessage).
     * @param url            Deobfuscated streaming url.
     */
    public static void setUrl(Object adaptiveFormat, String url) {
        if (adaptiveFormat != null) {
            try {
                Field field = adaptiveFormat.getClass().getField(FormatFields.url);
                field.setAccessible(true);
                field.set(adaptiveFormat, url);
            } catch (Exception ex) {
                Logger.printException(() -> "setUrl failed", ex);
            }
        }
    }
}
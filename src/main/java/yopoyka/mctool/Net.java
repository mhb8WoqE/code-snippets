package yopoyka.mctool;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleIndexedCodec;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;

import java.util.EnumMap;

public class Net extends SimpleNetworkWrapper {
    protected int id = 0;

    public Net(String channelName) {
        super(channelName);
    }

    public <M extends IMessage> Net message(Class<M> messageType) {
        messageDisc(messageType);
        return this;
    }

    public <M extends IMessage> int messageDisc(Class<M> messageType) {
        final TObjectByteHashMap<Class<? extends IMessage>> types = getTypes();
        final int disc;
        if (!types.containsKey(messageType)) {
            types.put(messageType, (byte) id);
            disc = id++;
        }
        else
            disc = types.get(messageType);

        return disc;
    }

    public <M extends IMessage> Net client(Class<M> requestType, IMessageHandler<M, ?> handler) {
        return sided(requestType, handler, Side.CLIENT);
    }

    public <M extends IMessage> Net server(Class<M> requestType, IMessageHandler<M, ?> handler) {
        return sided(requestType, handler, Side.SERVER);
    }

    public <M extends IMessage> Net sided(Class<M> requestType, IMessageHandler<M, ?> handler, Side side) {
        registerMessage(handler, requestType, messageDisc(requestType), side);
        return this;
    }

    protected EnumMap<Side, FMLEmbeddedChannel> getChannels() {
        return getPrivateValue(SimpleNetworkWrapper.class, this, "channels");
    }

    protected SimpleIndexedCodec getCodec() {
        return getPrivateValue(SimpleNetworkWrapper.class, this, "packetCodec");
    }

    protected TByteObjectHashMap<Class<? extends IMessage>> getDiscriminators() {
        return getPrivateValue(FMLIndexedMessageToMessageCodec.class, getCodec(), "discriminators");
    }

    protected TObjectByteHashMap<Class<? extends IMessage>> getTypes() {
        return getPrivateValue(FMLIndexedMessageToMessageCodec.class, getCodec(), "types");
    }

    @SuppressWarnings("unchecked")
    private static <T, C> T getPrivateValue(Class<C> clazz, C instance, String field) {
        try {
            final java.lang.reflect.Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            return (T) f.get(instance);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}

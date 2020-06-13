package chat.server.algorithms.mutex;

import chat.common.Action;
import chat.common.Entity;
import chat.common.MsgContent;
import chat.server.Server;

import java.util.Objects;
import java.util.function.BiConsumer;

public enum MutexAction implements Action {
    /**
     * the enumerator for the action of the request token message of the mutex algorithm.
     */
    REQUEST_TOKEN_MESSAGE(MutexRequestTokenContent.class,
            (Entity server, MsgContent content) -> ((Server) server)
                    .receiveMutexRequestTokenContent((MutexRequestTokenContent) content)),
    /**
     * the enumerator for the action of the send token message of the mutex algorithm.
     */
    SEND_TOKEN_MESSAGE(MutexSendTokenContent.class,
            (Entity server, MsgContent content) -> ((Server) server)
                    .receiveMutexSendTokenContent((MutexSendTokenContent) content));

    /**
     * the type of the content.
     */
    private final Class<? extends MsgContent> contentClass;

    /**
     * the lambda expression of the action.
     */
    private final BiConsumer<Entity, MsgContent> actionFunction;

    MutexAction(final Class<? extends MsgContent> contentClass, final BiConsumer<Entity, MsgContent> actionFunction) {
        Objects.requireNonNull(contentClass, "argument contentClass cannot be null");
        Objects.requireNonNull(actionFunction, "argument actionFunction cannot be null");
        this.contentClass = contentClass;
        this.actionFunction = actionFunction;
    }

    /**
     * gets the type of the message.
     *
     * @return the type of the message.
     */
    @Override
    public Class<? extends MsgContent> contentClass() {
        return contentClass;
    }

    /**
     * gets the lambda expression of the action to execute.
     *
     * @return the lambda expression of the action.
     */
    @Override
    public BiConsumer<Entity, MsgContent> actionFunction() {
        return actionFunction;
    }
}

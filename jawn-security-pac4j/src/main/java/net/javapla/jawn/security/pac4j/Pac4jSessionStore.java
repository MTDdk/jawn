package net.javapla.jawn.security.pac4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Optional;

import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.http.BadRequestAction;
import org.pac4j.core.exception.http.ForbiddenAction;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.NoContentAction;
import org.pac4j.core.exception.http.OkAction;
import org.pac4j.core.exception.http.SeeOtherAction;
import org.pac4j.core.exception.http.StatusAction;
import org.pac4j.core.exception.http.TemporaryRedirectAction;
import org.pac4j.core.exception.http.UnauthorizedAction;
import org.pac4j.core.exception.http.WithContentAction;
import org.pac4j.core.exception.http.WithLocationAction;

import net.javapla.jawn.core.Session;
import net.javapla.jawn.core.Up;
import net.javapla.jawn.core.Value;

public class Pac4jSessionStore implements org.pac4j.core.context.session.SessionStore<Pac4jContext> {
    
    private static final String PAC4J = "p4j~";
    private static final String BIN = "b64~";

    @Override
    public String getOrCreateSessionId(Pac4jContext context) {
        return getSession(context).getId();
    }

    @Override
    public Optional<Object> get(Pac4jContext context, String key) {
        Optional<Object> sessionValue = getSessionOrEmpty(context)
            .map(session -> session.get(key))
            .map(Pac4jSessionStore::strToObject)
            .orElseGet(Optional::empty);
        return sessionValue;
    }

    @Override
    public void set(Pac4jContext context, String key, Object value) {
        if (value == null || value.toString().length() == 0) {
            getSessionOrEmpty(context).ifPresent(session -> session.remove(key));
        } else {
            String encoded = objToStr(value);
            getSession(context).put(key, encoded);
        }
    }

    @Override
    public boolean destroySession(Pac4jContext context) {
        Optional<Session> session = getSessionOrEmpty(context);
        session.ifPresent(Session::invalidate);
        return session.isPresent();
    }

    @Override
    public Optional<Session> getTrackableSession(Pac4jContext context) {
        return getSessionOrEmpty(context);
    }

    @Override
    public Optional<SessionStore<Pac4jContext>> buildFromTrackableSession(Pac4jContext context, Object trackableSession) {
        if (trackableSession != null) {
            return Optional.of(new Pac4jSessionStore());
        }
        return Optional.empty();
    }

    @Override
    public boolean renewSession(Pac4jContext context) {
        getSessionOrEmpty(context).ifPresent(session -> {
            // renew id of session
            // includes removing from sessionStore and adding the same session under a new id
            // updateState of session
        });
        return true;
    }

    private Optional<Session> getSessionOrEmpty(Pac4jContext context) {
        return context.context().sessionOptionally();
    }
    
    private Session getSession(Pac4jContext context) {
        return context.context().session();
    }
    
    static Optional<Object> strToObject(final Value node) {
        if (!node.isPresent()) {
            return Optional.empty();
        }
        String value = node.value();
        if (value.startsWith(BIN)) {
            try {
                byte[] bytes = Base64.getDecoder().decode(value.substring(BIN.length()));
                return Optional.of(new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject());
            } catch (Exception x) {
                throw Up.IO.because(x);
            }
        } else if (value.startsWith(PAC4J)) {
            return Optional.of(strToAction(value.substring(PAC4J.length())));
        }
        return Optional.of(value);
    }
    
    static String objToStr(final Object value) {
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else if (value instanceof HttpAction) {
            return actionToStr((HttpAction) value);
        }
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(bytes);
            stream.writeObject(value);
            stream.flush();
            return BIN + Base64.getEncoder().encodeToString(bytes.toByteArray());
        } catch (IOException x) {
            throw Up.IO.because(x);
        }
    }
    
    private static String actionToStr(HttpAction action) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(PAC4J).append(action.getCode());
        if (action instanceof WithContentAction) {
            buffer.append(":").append(((WithContentAction) action).getContent());
        } else if (action instanceof WithLocationAction) {
            buffer.append(":").append(((WithLocationAction) action).getLocation());
        }
        return buffer.toString();
    }
    
    private static HttpAction strToAction(String value) {
        int i = value.indexOf(":");
        int code;
        String tail;
        if (i > 0) {
            code = Integer.parseInt(value.substring(0, i));
            tail = value.substring(i + 1);
        } else {
            code = Integer.parseInt(value);
            tail = null;
        }
        switch (code) {
            case HttpConstants.BAD_REQUEST:
                return BadRequestAction.INSTANCE;
            case HttpConstants.FORBIDDEN:
                return ForbiddenAction.INSTANCE;
            case HttpConstants.FOUND:
                return new FoundAction(tail);
            case HttpConstants.NO_CONTENT:
                return NoContentAction.INSTANCE;
            case HttpConstants.OK:
                return new OkAction(tail);
            case HttpConstants.TEMPORARY_REDIRECT:
                return new TemporaryRedirectAction(tail);
            case HttpConstants.SEE_OTHER:
                return new SeeOtherAction(tail);
            case HttpConstants.UNAUTHORIZED:
                return UnauthorizedAction.INSTANCE;
            default:
                return new StatusAction(code);
        }
    }
}

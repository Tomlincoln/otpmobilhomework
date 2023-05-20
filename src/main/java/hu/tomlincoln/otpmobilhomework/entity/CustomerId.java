package hu.tomlincoln.otpmobilhomework.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

@Embeddable
public class CustomerId implements Serializable {

    private static final long serialVersionUID = 1L;

    private String webshopId;
    private String userId;

    public CustomerId() {
    }

    public CustomerId(String webshopId, String userId) {
        this.webshopId = webshopId;
        this.userId = userId;
    }

    public String getWebshopId() {
        return webshopId;
    }

    public void setWebshopId(String webshopId) {
        this.webshopId = webshopId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomerId that = (CustomerId) o;

        if (!Objects.equals(webshopId, that.webshopId)) return false;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        int result = webshopId != null ? webshopId.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }
}

package server;

import com.google.gson.JsonElement;

public class Response {

    private String response;
    private JsonElement value;
    private String reason;

    public static Builder builder() {
        return new Builder();
    }

    public String getResponse() {
        return response;
    }

    private void setResponse(String response) {
        this.response = response;
    }

    public JsonElement getValue() {
        return value;
    }

    private void setValue(JsonElement value) {
        this.value = value;
    }

    public String getReason() {
        return reason;
    }

    private void setReason(String reason) {
        this.reason = reason;
    }

    public static class Builder {
        private Response response;

        public Builder() {
            this.response = new Response();
        }

        public Builder response(String response) {
            this.response.setResponse(response);
            return this;
        }

        public Builder value(JsonElement value) {
            this.response.setValue(value);
            return this;
        }

        public Builder reason(String reason) {
            this.response.setReason(reason);
            return this;
        }

        public Response build() {
            return response;
        }
    }
}

#define _QNX_SOURCE
#define _GNU_SOURCE

#include <inttypes.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include "sysrepo.h"

volatile int exit_application = 0;

#define MAX_STRING 256
#define PORT 5055

void stringify_value(sr_val_t *val, char *buffer, size_t bufsize) {
    if (!val) {
        snprintf(buffer, bufsize, "null");
        return;
    }

    switch (val->type) {
        case SR_STRING_T:
            snprintf(buffer, bufsize, "%s", val->data.string_val);
            break;
        case SR_BOOL_T:
            snprintf(buffer, bufsize, "%s", val->data.bool_val ? "true" : "false");
            break;
        case SR_DECIMAL64_T:
            snprintf(buffer, bufsize, "%g", val->data.decimal64_val);
            break;
        case SR_IDENTITYREF_T:
            snprintf(buffer, bufsize, "%s", val->data.identityref_val);
            break;
        case SR_ENUM_T:
            snprintf(buffer, bufsize, "%s", val->data.enum_val);
            break;
        case SR_INT32_T:
            snprintf(buffer, bufsize, "%" PRId32, val->data.int32_val);
            break;
        case SR_UINT32_T:
            snprintf(buffer, bufsize, "%" PRIu32, val->data.uint32_val);
            break;
        case SR_CONTAINER_T:
            snprintf(buffer, bufsize, "(container)");
            break;
        case SR_CONTAINER_PRESENCE_T:
            snprintf(buffer, bufsize, "(presence-container)");
            break;
        case SR_LIST_T:
            snprintf(buffer, bufsize, "(list)");
            break;
        case SR_LEAF_EMPTY_T:
            snprintf(buffer, bufsize, "(empty)");
            break;
        case SR_BITS_T:
            snprintf(buffer, bufsize, "%s", val->data.bits_val);
            break;
        case SR_BINARY_T:
            snprintf(buffer, bufsize, "%s", val->data.binary_val);
            break;
        case SR_INSTANCEID_T:
            snprintf(buffer, bufsize, "%s", val->data.instanceid_val);
            break;
        case SR_INT8_T:
            snprintf(buffer, bufsize, "%" PRId8, val->data.int8_val);
            break;
        case SR_INT16_T:
            snprintf(buffer, bufsize, "%" PRId16, val->data.int16_val);
            break;
        case SR_INT64_T:
            snprintf(buffer, bufsize, "%" PRId64, val->data.int64_val);
            break;
        case SR_UINT8_T:
            snprintf(buffer, bufsize, "%" PRIu8, val->data.uint8_val);
            break;
        case SR_UINT16_T:
            snprintf(buffer, bufsize, "%" PRIu16, val->data.uint16_val);
            break;
        case SR_UINT64_T:
            snprintf(buffer, bufsize, "%" PRIu64, val->data.uint64_val);
            break;
        default:
            snprintf(buffer, bufsize, "(unknown-type %d)", val->type);
            break;
    }
}

void send_request(sr_val_t *old_value, sr_val_t *new_value, int type) {
    int sockfd;
    struct sockaddr_in servaddr;
    char o_value[MAX_STRING] = "";
    char n_value[MAX_STRING] = "";
    const char *xpath = NULL;

    if (type == 0) {  // MODIFIED
        if (!old_value || !new_value || !new_value->xpath) {
            printf("⚠️ Skipped: Invalid values for modify.\n");
            return;
        }
        xpath = new_value->xpath;
        stringify_value(old_value, o_value, MAX_STRING);
        stringify_value(new_value, n_value, MAX_STRING);
    }
    else if (type == 1) {  // CREATED
        if (!new_value || !new_value->xpath) {
            printf("⚠️ Skipped: Invalid values for create.\n");
            return;
        }
        xpath = new_value->xpath;
        strncpy(o_value, "creation", MAX_STRING - 1);
        strncpy(n_value, "new_link", MAX_STRING - 1);
    }
    else if (type == 2) {  // DELETED
        if (!old_value || !old_value->xpath) {
            printf("⚠️ Skipped: Invalid values for delete.\n");
            return;
        }
        xpath = old_value->xpath;
        stringify_value(old_value, o_value, MAX_STRING);
        strncpy(n_value, "deleted", MAX_STRING - 1);
    } else {
        printf("⚠️ Skipped: Unknown type.\n");
        return;
    }

    char string_to_send[1024];
    snprintf(string_to_send, sizeof(string_to_send),
        "{ \"parameter_name\": \"%s\", \"old_value\": \"%s\", \"new_value\": \"%s\" }",
        xpath, o_value, n_value);

    printf("\U0001F4E4 Sending to Python Server:\n%s\n", string_to_send);

    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        perror("❌ Socket creation failed");
        return;
    }

    memset(&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = inet_addr("127.0.0.1");
    servaddr.sin_port = htons(PORT);

    if (connect(sockfd, (struct sockaddr *)&servaddr, sizeof(servaddr)) < 0) {
        perror("❌ Connection to server failed");
        close(sockfd);
        return;
    }

    if (send(sockfd, string_to_send, strlen(string_to_send), 0) < 0) {
        perror("❌ Sending failed");
    } else {
        printf("✅ Message sent successfully.\n");
    }
    close(sockfd);
}

const char *ev_to_str(sr_event_t ev) {
    switch (ev) {
        case SR_EV_CHANGE: return "change";
        case SR_EV_DONE: return "done";
        case SR_EV_ABORT: default: return "abort";
    }
}

static int module_change_cb(sr_session_ctx_t *session, uint32_t sub_id, const char *module_name, const char *xpath,
                             sr_event_t event, uint32_t request_id, void *private_data) {
    sr_change_iter_t *it = NULL;
    int rc = SR_ERR_OK;
    char path[512];
    sr_change_oper_t oper;
    sr_val_t *old_value = NULL;
    sr_val_t *new_value = NULL;

    printf("\n\n========== EVENT %s CHANGES: ==========" "\n\n", ev_to_str(event));

    if (xpath) {
        snprintf(path, sizeof(path), "%s//.", xpath);
    } else {
        snprintf(path, sizeof(path), "/%s:*//.", module_name);
    }

    rc = sr_get_changes_iter(session, path, &it);
    if (rc != SR_ERR_OK) {
        goto cleanup;
    }

    while ((rc = sr_get_change_next(session, it, &oper, &old_value, &new_value)) == SR_ERR_OK) {
        switch (oper) {
            case SR_OP_CREATED:
                if (strcmp(ev_to_str(event), "done") == 0)
                    send_request(NULL, new_value, 1);
                break;
            case SR_OP_MODIFIED:
                if (strcmp(ev_to_str(event), "done") == 0)
                    send_request(old_value, new_value, 0);
                break;
            case SR_OP_DELETED:
                if (strcmp(ev_to_str(event), "done") == 0)
                    send_request(old_value, NULL, 2);
                break;
            default:
                break;
        }
        sr_free_val(old_value);
        sr_free_val(new_value);
    }

cleanup:
    sr_free_change_iter(it);
    return SR_ERR_OK;
}

static void sigint_handler(int signum) {
    (void)signum;
    exit_application = 1;
}

int main(int argc, char **argv) {
    sr_conn_ctx_t *connection = NULL;
    sr_session_ctx_t *session = NULL;
    sr_subscription_ctx_t *subscription = NULL;
    int rc = SR_ERR_OK;

    if (argc < 2) {
        printf("Usage: %s <module-name>\n", argv[0]);
        return EXIT_FAILURE;
    }

    sr_log_stderr(SR_LL_ERR);

    rc = sr_connect(0, &connection);
    if (rc != SR_ERR_OK) return EXIT_FAILURE;

    rc = sr_session_start(connection, SR_DS_RUNNING, &session);
    if (rc != SR_ERR_OK) goto cleanup;

    rc = sr_module_change_subscribe(session, argv[1], NULL, module_change_cb, NULL, 0, 0, &subscription);
    if (rc != SR_ERR_OK) goto cleanup;

    printf("\nListening for changes on module: %s\n", argv[1]);

    signal(SIGINT, sigint_handler);
    signal(SIGPIPE, SIG_IGN);
    while (!exit_application) {
        sleep(1);
    }

cleanup:
    sr_disconnect(connection);
    return rc ? EXIT_FAILURE : EXIT_SUCCESS;
}

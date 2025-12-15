/**
 * @file application_changes_example.c
 * @author Michal Vasko <mvasko@cesnet.cz>
 * @brief example of an application handling changes
 *
 * @copyright
 * Copyright (c) 2019 - 2022 CESNET, z.s.p.o.
 *
 * This source code is licensed under BSD 3-Clause License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://opensource.org/licenses/BSD-3-Clause
 */

#define _QNX_SOURCE /* sleep() */
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

static void
print_val(const sr_val_t *value)
{
    if (NULL == value) {
        return;
    }

    printf("%s ", value->xpath);

    switch (value->type) {
    case SR_CONTAINER_T:
    case SR_CONTAINER_PRESENCE_T:
        printf("(container)");
        break;
    case SR_LIST_T:
        printf("(list instance)");
        break;
    case SR_STRING_T:
        printf("= %s", value->data.string_val);
        break;
    case SR_BOOL_T:
        printf("= %s", value->data.bool_val ? "true" : "false");
        break;
    case SR_DECIMAL64_T:
        printf("= %g", value->data.decimal64_val);
        break;
    case SR_INT8_T:
        printf("= %" PRId8, value->data.int8_val);
        break;
    case SR_INT16_T:
        printf("= %" PRId16, value->data.int16_val);
        break;
    case SR_INT32_T:
        printf("= %" PRId32, value->data.int32_val);
        break;
    case SR_INT64_T:
        printf("= %" PRId64, value->data.int64_val);
        break;
    case SR_UINT8_T:
        printf("= %" PRIu8, value->data.uint8_val);
        break;
    case SR_UINT16_T:
        printf("= %" PRIu16, value->data.uint16_val);
        break;
    case SR_UINT32_T:
        printf("= %" PRIu32, value->data.uint32_val);
        break;
    case SR_UINT64_T:
        printf("= %" PRIu64, value->data.uint64_val);
        break;
    case SR_IDENTITYREF_T:
        printf("= %s", value->data.identityref_val);
        break;
    case SR_INSTANCEID_T:
        printf("= %s", value->data.instanceid_val);
        break;
    case SR_BITS_T:
        printf("= %s", value->data.bits_val);
        break;
    case SR_BINARY_T:
        printf("= %s", value->data.binary_val);
        break;
    case SR_ENUM_T:
        printf("= %s", value->data.enum_val);
        break;
    case SR_LEAF_EMPTY_T:
        printf("(empty leaf)");
        break;
    default:
        printf("(unprintable)");
        break;
    }

    switch (value->type) {
    case SR_UNKNOWN_T:
    case SR_CONTAINER_T:
    case SR_CONTAINER_PRESENCE_T:
    case SR_LIST_T:
    case SR_LEAF_EMPTY_T:
        printf("\n");
        break;
    default:
        printf("%s\n", value->dflt ? " [default]" : "");
        break;
    }
}




static int check_qkdl_id_role( const char *target_qkdl_id, sr_session_ctx_t *session, const char *module_name) {
    sr_val_t *values = NULL;
    size_t count = 0;
    int rc = SR_ERR_OK;
    char *xpath;

    // Fetch all items from the module
    if (asprintf(&xpath, "/%s:*//.", module_name) == -1) {
        return SR_ERR_NO_MEMORY;
    }
    rc = sr_get_items(session, xpath, 0, 0, &values, &count);
    free(xpath);
    if (rc != SR_ERR_OK) {
        return rc;
    }

    int found = 0; // Flag to mark if the target qkdl_id with TRANSMITTER role is found
    for (size_t i = 0; i < count; i++) {
        if (values[i].type == SR_STRING_T && strstr(values[i].xpath, "qkd_link[qkdl_id=") != NULL) {
            // Extract qkdl_id from the xpath
            char qkdl_id[256]; // Adjust size according to expected ID length
            if (sscanf(values[i].xpath, "/%*[^:]:qkd_node/qkd_links/qkd_link[qkdl_id='%255[^']']", qkdl_id) == 1) {
                if (strcmp(qkdl_id, target_qkdl_id) == 0) {
                    // Target qkdl_id found, now check for its role in subsequent items
                    for (size_t j = i + 1; j < count; j++) {
                        if (strstr(values[j].xpath, "phys_qkd_role") != NULL && strstr(values[j].data.string_val, "TRANSMITTER") != NULL) {
                            found = 1;
                            break;
                        }
                    }
                }
            }
        }
        if (found) break; // Exit loop if target qkdl_id with TRANSMITTER role is found
    }

    sr_free_values(values, count);

    if (found) {
        printf("qkdl_id '%s' has the role TRANSMITTER.\n", target_qkdl_id);
        return 1;
    } else {
        printf("qkdl_id '%s' does not have the role TRANSMITTER or was not found.\n", target_qkdl_id);
        return 0;
    }


}








int extractQkdlId(const char *str,sr_session_ctx_t *session, const char *module_name) {
    const char *key = "qkdl_id='";
    const char *start, *end;

    // Find the start of the 'qkdl_id' substring
    start = strstr(str, key);
    if (start != NULL) {
        // Move past the key to the beginning of the actual ID
        start += strlen(key);
        // Find the end of the ID, marked by the next '
        end = strchr(start, '\'');
        if (end != NULL) {
            // Calculate the length of the ID
            ptrdiff_t idLength = end - start;
            // Allocate memory for the ID (+1 for the null terminator)
            char *qkdl_id = (char *)malloc(idLength + 1);
            if (qkdl_id != NULL) {
                // Copy the ID into our new string
                strncpy(qkdl_id, start, idLength);
                // Null-terminate the string
                qkdl_id[idLength] = '\0';
                // Output the ID
                printf("qkdl_id: %s\n", qkdl_id);

                // CHECK IF THE ID IS A TRANSMITTER LINK
                return check_qkdl_id_role(qkdl_id,session, module_name);
                    

                // Free the allocated memory
                free(qkdl_id);
            } else {
                printf("Memory allocation failed.\n");
            }
        } else {
            printf("End of qkdl_id not found.\n");
        }
    } else {
        printf("qkdl_id not found in the string.\n");
    }
    return -1;
}

// type = 0 -> modifica di un elemento
// type = 1 -> creazione
void send_request(sr_val_t *old_value, sr_val_t *new_value, int type,sr_session_ctx_t *session, const char *module_name){
    int sockfd;
    struct sockaddr_in servaddr;
    int PORT = 5000;

    int MAX_STRING = 50;
    char o_value[MAX_STRING];
    char n_value[MAX_STRING];

    if (type == 0){

        
        if(old_value->type== SR_STRING_T){
            printf("Sono qui, devo controllare che sia un link e che sia transmitter\n");
            strcpy(o_value,old_value->data.string_val);
            strcpy(n_value,new_value->data.string_val);
        }

        if(old_value->type==SR_DECIMAL64_T){
            char tmp[MAX_STRING];
            sprintf(tmp,"%g",old_value->data.decimal64_val);
            //itoa(old_value->data.decimal64_val,tmp,10);
            strcpy(o_value,tmp);
            char tmp_2[MAX_STRING];
            // strcpy(new_value->data.decimal64_val,tmp_2,10);
            sprintf(tmp_2,"%g",new_value->data.decimal64_val);
            strcpy(n_value,tmp_2);
        }
        if(old_value->type==SR_IDENTITYREF_T){

            // TODO: check that the modified value is a link and that is a transmitter link
            
            char tmp[MAX_STRING];
            sprintf(tmp,"%s",old_value->data.identityref_val);
        
            strcpy(o_value,tmp);
            char tmp_2[MAX_STRING];
            // strcpy(new_value->data.decimal64_val,tmp_2,10);
            sprintf(tmp_2,"%s",new_value->data.identityref_val);
            strcpy(n_value,tmp_2);
            if(extractQkdlId(new_value->xpath,session, module_name)==0){
                printf("NO CHANGES NEED TO BE SENT TO THE PYTHON SERVER SINCE THE ROLE IS RECEIVER\n");
                return;
            }
        }
        
        
    } 
    if (type==1){
        strcpy(o_value,"creation");
        strcpy(n_value,"Nuovo link");
    }

    // xpath: /etsi-qkd-sdn-node:qkd_node/qkd_links/qkd_link[qkdl_id='f81d4fae-aaaa-aaaa-aaaa-00a0c91e6bf2']

    char string_to_send[1024];
    strcpy(string_to_send,"parameter_name:");
    strcat(string_to_send,new_value->xpath);
    char old_value_string[100];
    strcpy(old_value_string, ",old_value:");
    strcat(old_value_string,o_value);
    strcat(string_to_send,old_value_string);
    char new_value_string[100];
    strcpy(new_value_string, ",new_value:");
    strcat(new_value_string,n_value);
    strcat(string_to_send,new_value_string);

    printf("string to send:\n");
    printf("%s",string_to_send);
    // Create a socket
    sockfd = socket(AF_INET, SOCK_STREAM, 0);

    // Set server address and port
    memset(&servaddr, 0, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = inet_addr("127.0.0.1");
    servaddr.sin_port = htons(PORT);

    // Connect to the server
    connect(sockfd, (struct sockaddr *)&servaddr, sizeof(servaddr));

    // Send data to the server
    send(sockfd, string_to_send, strlen(string_to_send), 0);

    // Close the socket
    close(sockfd);
}

static void
print_change(sr_change_oper_t op, sr_val_t *old_val, sr_val_t *new_val)
{
    switch (op) {
    case SR_OP_CREATED:
        printf("CREATED: ");
        print_val(new_val);
        //TODO : gestire qui la creazione di una nuova sezione
        break;
    case SR_OP_DELETED:
        printf("DELETED: ");
        print_val(old_val);
        break;
    case SR_OP_MODIFIED:
        printf("MODIFIED: ");
        print_val(old_val);
        printf("to ");
        print_val(new_val);
        break;
    case SR_OP_MOVED:
        printf("MOVED: %s\n", new_val->xpath);
        break;
    }
}

static int
print_current_config(sr_session_ctx_t *session, const char *module_name)
{
    sr_val_t *values = NULL;
    size_t count = 0;
    int rc = SR_ERR_OK;
    char *xpath;

    if (asprintf(&xpath, "/%s:*//.", module_name) == -1) {
        return SR_ERR_NO_MEMORY;
    }
    rc = sr_get_items(session, xpath, 0, 0, &values, &count);
    free(xpath);
    if (rc != SR_ERR_OK) {
        return rc;
    }

    for (size_t i = 0; i < count; i++) {
        print_val(&values[i]);
    }
    sr_free_values(values, count);

    return rc;
}

const char *
ev_to_str(sr_event_t ev)
{
    switch (ev) {
    case SR_EV_CHANGE:
        return "change";
    case SR_EV_DONE:
        return "done";
    case SR_EV_ABORT:
    default:
        return "abort";
    }
}


static int
module_change_cb(sr_session_ctx_t *session, uint32_t sub_id, const char *module_name, const char *xpath, sr_event_t event,
        uint32_t request_id, void *private_data)
{
    sr_change_iter_t *it = NULL;
    int rc = SR_ERR_OK;
    char path[512];
    sr_change_oper_t oper;
    sr_val_t *old_value = NULL;
    sr_val_t *new_value = NULL;

    (void)sub_id;
    (void)request_id;
    (void)private_data;

    printf("\n\n ========== EVENT %s CHANGES: ====================================\n\n", ev_to_str(event));

    if (xpath) {
        sprintf(path, "%s//.", xpath);
    } else {
        sprintf(path, "/%s:*//.", module_name);
    }
    rc = sr_get_changes_iter(session, path, &it);
    if (rc != SR_ERR_OK) {
        goto cleanup;
    }
    int done = 0;
    while ((rc = sr_get_change_next(session, it, &oper, &old_value, &new_value)) == SR_ERR_OK) {
        print_change(oper, old_value, new_value);
        
        switch (oper) {
            case SR_OP_CREATED:
                if((strcmp(ev_to_str(event),"done") == 0 && done==0) ){
                    printf("SONO NEL CREATED: ");
                    send_request(NULL,new_value, 1,session, module_name);
                    done = 1;
                }
                break;
            case SR_OP_MODIFIED:
                if((strcmp(ev_to_str(event),"done") == 0) ){
                    printf("SONO NEL modified: ");
                    send_request(old_value,new_value,0,session, module_name);
                    sr_free_val(old_value);
                    sr_free_val(new_value);
                }                
                
                break;
            }
        /*
        // send data to python server
        if((strcmp(ev_to_str(event),"change") == 0) ){
            
            sr_free_val(old_value);
            sr_free_val(new_value);
        }
        if((strcmp(ev_to_str(event),"change") == 0) ){
            send_request(old_value,new_value);
        }
        */
            
        
    }

    printf("\n ========== END OF CHANGES =======================================");

    if (event == SR_EV_DONE) {
        printf("\n\n ========== CONFIG HAS CHANGED, CURRENT RUNNING CONFIG: ==========\n\n");
        if (print_current_config(session, module_name) != SR_ERR_OK) {
            goto cleanup;
        }
    }

cleanup:
    sr_free_change_iter(it);
    return SR_ERR_OK;
}

static void
sigint_handler(int signum)
{
    (void)signum;

    exit_application = 1;
}

static const char *
ds2str(sr_datastore_t ds)
{
    switch (ds) {
    case SR_DS_RUNNING:
        return "running";
    case SR_DS_OPERATIONAL:
        return "operational";
    case SR_DS_STARTUP:
        return "startup";
    case SR_DS_CANDIDATE:
        return "candidate";
    default:
        return NULL;
    }
}

static int
str2ds(const char *str, sr_datastore_t *ds)
{
    if (!strcmp(str, "running")) {
        *ds = SR_DS_RUNNING;
    } else if (!strcmp(str, "operational")) {
        *ds = SR_DS_OPERATIONAL;
    } else if (!strcmp(str, "startup")) {
        *ds = SR_DS_STARTUP;
    } else if (!strcmp(str, "candidate")) {
        *ds = SR_DS_CANDIDATE;
    } else {
        return 1;
    }

    return 0;
}

int
main(int argc, char **argv)
{
    sr_conn_ctx_t *connection = NULL;
    sr_session_ctx_t *session = NULL;
    sr_subscription_ctx_t *subscription = NULL;
    int rc = SR_ERR_OK;
    const char *mod_name, *xpath = NULL;
    sr_datastore_t ds = SR_DS_RUNNING;

    if ((argc < 2) || (argc > 4)) {
        printf("%s <module-to-subscribe> [<xpath-to-subscribe>] [startup/running/operational/candidate]\n", argv[0]);
        return EXIT_FAILURE;
    }
    mod_name = argv[1];
    if (argc > 2) {
        if (str2ds(argv[2], &ds)) {
            /* 2nd arg xpath */
            xpath = argv[2];
        }
    }
    if (argc > 3) {
        if (str2ds(argv[3], &ds)) {
            printf("Invalid datastore %s\n", argv[3]);
            return EXIT_FAILURE;
        }
    }

    printf("Application will watch for \"%s\" changes in \"%s\" datastore.\n", xpath ? xpath : mod_name, ds2str(ds));

    /* turn logging on */
    sr_log_stderr(SR_LL_WRN);

    /* connect to sysrepo */
    rc = sr_connect(0, &connection);
    if (rc != SR_ERR_OK) {
        goto cleanup;
    }

    /* start session */
    rc = sr_session_start(connection, ds, &session);
    if (rc != SR_ERR_OK) {
        goto cleanup;
    }

    /* read current config */
    printf("\n ========== READING RUNNING CONFIG: ==========\n\n");
    if (print_current_config(session, mod_name) != SR_ERR_OK) {
        goto cleanup;
    }

    /* subscribe for changes in running config */
    rc = sr_module_change_subscribe(session, mod_name, xpath, module_change_cb, NULL, 0, 0, &subscription);
    if (rc != SR_ERR_OK) {
        goto cleanup;
    }

    printf("\n\n ========== LISTENING FOR CHANGES ==========\n\n");

    /* loop until ctrl-c is pressed / SIGINT is received */
    signal(SIGINT, sigint_handler);
    signal(SIGPIPE, SIG_IGN);
    while (!exit_application) {
        sleep(1000);
    }

    printf("Application exit requested, exiting.\n");

cleanup:
    sr_disconnect(connection);
    return rc ? EXIT_FAILURE : EXIT_SUCCESS;
}

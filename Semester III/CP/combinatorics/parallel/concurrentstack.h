#pragma once

#include "task.h"

#include <pthread.h>

typedef struct {
    pthread_mutex_t lock;
    pthread_cond_t new_element;

    int thread_count;
    int wait_cnt;
    bool finished;

    int capacity;
    int element_count;

    Task* elements;
} ConcurrentStack;

void create_stack(ConcurrentStack* s, int thread_count);

void push(ConcurrentStack* s, Task t);

Task pop(ConcurrentStack* s, bool* finished);

void destroy_stack(ConcurrentStack* s);
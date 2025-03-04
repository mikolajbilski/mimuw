#pragma once

#include "common/sumset.h"

typedef struct {
    Sumset* a;
    Sumset* b;
    Sumset* prev_array;
} Task;

void create_task(Task* t, Sumset* a, Sumset* b);

void destroy_task(Task* t);
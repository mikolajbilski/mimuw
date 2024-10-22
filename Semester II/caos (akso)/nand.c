#include <errno.h>
#include <stdlib.h>
#include <stdint.h>
#include "nand.h"

typedef struct input {
    const void *input_signal; // pointer to either a nand gate, or a boolean signal
    bool is_boolean; // used to determine what kind of input it is
} input_t;

// each gate's output can be connected to up to MAX_OUTPUT_COUNT other gate's inputs
static const size_t MAX_OUTPUT_COUNT = SIZE_MAX / sizeof(nand_t*);

struct nand {
    const input_t **inputs;
    nand_t **outputs;
    size_t input_count;
    size_t output_count; // how many outputs there actually are
    size_t output_capacity; // how big is the outputs array

    bool result;
    size_t cpl; // critical path length
    bool is_on_stack;
    bool visited;
};

// reset a single gate after evaluation or creation
static void reset_gate(nand_t *g) {
    if (g == NULL) {
        errno = EINVAL;
        return;
    }

    g->cpl = 0;
    g->visited = false;
    g->result = false;
    g->is_on_stack = false;
}

nand_t *nand_new(unsigned n) {
    nand_t *gate = (nand_t*) malloc(sizeof(nand_t));
    if (gate == NULL) {
        errno = ENOMEM;
        return NULL;
    }

    gate->output_count = 0;
    gate->output_capacity = 0;
    gate->outputs = NULL;
    reset_gate(gate);

    gate->input_count = n;
    if (n == 0) {
        gate->inputs = NULL;
        return gate;
    }

    gate->inputs = (const input_t**) malloc(n  *sizeof(input_t*));
    if (gate->inputs == NULL) {
        free(gate);
        errno = ENOMEM;
        return NULL;
    }
    for (unsigned i = 0; i < n; ++i) {
        gate->inputs[i] = NULL;
    }

    return gate;
}

static size_t max(size_t a, size_t b) {
    return (a > b) ? a : b;
}

// remove g from g_in's list of inputs
// 0 - OK
// -1 - either argument is NULL or g is not in g_in's list of inputs (errno = EINVAL)
static int unpin_input(nand_t *const g_in, void *const g) {
    if (g == NULL || g_in == NULL) {
        errno = EINVAL;
        return -1;
    }

    bool found = false;
    for (size_t i = 0; i < g_in->input_count; ++i) {
        if (g_in->inputs[i] != NULL && g_in->inputs[i]->input_signal == g) {
            // found it, now delete it
            found = true;
            free((input_t*) g_in->inputs[i]);
            g_in->inputs[i] = NULL;
            break;
        }
    }

    if (!found) {
        errno = EINVAL;
        return -1;
    }
    return 0;
}

// remove g from g_out's list of outputs
// 0 - OK
// -1 - either argument is NULL or g is not in g_out's list of outputs (errno = EINVAL)
static int unpin_output(nand_t *const g_out, nand_t *const g) {
    if (g == NULL || g_out == NULL || g_out->outputs == NULL || g_out->output_count == 0) {
        errno = EINVAL;
        return -1;
    } 

    bool found = false;
    for (size_t i = 0; i < g_out->output_count; ++i) {
        if (g_out->outputs[i] == g) {
            found = true;
            g_out->outputs[i] = g_out->outputs[g_out->output_count - 1];
            break;
        }
    }

    if (!found) {
        errno = EINVAL;
        return -1;
    }

    if (--g_out->output_count == 0) { // there are no more outputs - delete output array
        free(g_out->outputs);
        g_out->outputs = NULL;
        g_out->output_capacity = 0;
    } else if (g_out->output_count < g_out->output_capacity / 2) { // resize array when needed
        g_out->output_capacity /= 2;
        g_out->outputs = (nand_t**) realloc(g_out->outputs, sizeof(nand_t*)  *g_out->output_capacity);
    }
    return 0;
}

// add g to g_out's list of outputs
// 0 - OK
// -1 - no memory (errno = ENOMEM) or either argument is NULL or trying to add more outputs than SIZE_MAX (errno = EINVAL)
static int add_output(nand_t *const g_out, nand_t *const g) {
    if (g_out == NULL || g == NULL || g_out->output_count == MAX_OUTPUT_COUNT) {
        errno = EINVAL;
        return -1;
    }

    if (g_out->outputs == NULL) {
        g_out->outputs = (nand_t**) malloc(2  *sizeof(nand_t*));
        if (g_out->outputs == NULL) {
            errno = ENOMEM;
            return -1;
        }
        g_out->output_capacity = 2;
    }

    if (g_out->output_count == g_out->output_capacity) {
        size_t new_capacity = g_out->output_capacity;
        if (g_out->output_capacity >= MAX_OUTPUT_COUNT / 2) {
            new_capacity = MAX_OUTPUT_COUNT;
        } else {
            new_capacity *= 2;
        }
        nand_t **outputs = (nand_t**) realloc(g_out->outputs, sizeof(nand_t*)  *new_capacity);
        if (outputs == NULL) {
            errno = ENOMEM;
            return -1;
        }
        g_out->outputs = outputs;
        g_out->output_capacity = new_capacity;
    }
    g_out->outputs[g_out->output_count++] = g;
    return 0;
}

// create input_t when connecting a new input to a gate
static input_t *new_input(void *const input_signal, bool is_boolean) {
    input_t *new_input = (input_t*) malloc(sizeof(input_t));
    if (new_input != NULL) {
        new_input->input_signal = input_signal;
        new_input->is_boolean = is_boolean;
    }
    return new_input;
}

void nand_delete(nand_t *g) {
    if (g == NULL) return;

    if (g->inputs != NULL) {
        for (size_t i = 0; i < g->input_count; ++i) {
            if (g->inputs[i] != NULL) { 
                if (!g->inputs[i]->is_boolean) { // this input is another gate
                    unpin_output((nand_t*)g->inputs[i]->input_signal, g);
                }
                free((input_t*) g->inputs[i]);
            }
        }
        free(g->inputs);
    }
    if (g->outputs != NULL) {
        for (size_t i = 0; i < g->output_count; ++i) {
            unpin_input(g->outputs[i], g);
        }
        free(g->outputs);
    }
    free(g);
}

static int nand_connect_input(void *s, nand_t *g, unsigned k, bool is_boolean) {
    if (s == NULL || g == NULL || k >= g->input_count) {
        errno = EINVAL;
        return -1;
    }
    if (g->inputs[k] != NULL && !g->inputs[k]->is_boolean) {
        unpin_output((nand_t*)g->inputs[k]->input_signal, g);
    }
    const input_t *input = new_input((void*)s, is_boolean);
    if (input == NULL) {
        errno = ENOMEM;
        return -1;
    }
    if (g->inputs[k] != NULL) {
        free((void*) g->inputs[k]);
    }
    g->inputs[k] = input;
    return 0;
}

int nand_connect_nand(nand_t *g_out, nand_t *g_in, unsigned k) {
    int out = nand_connect_input(g_out, g_in, k, false);
    if (out != -1) add_output(g_out, g_in);
    return out;
}

int nand_connect_signal(bool const *s, nand_t *g, unsigned k) {
    return nand_connect_input((void*) s, g, k, true);;
}

// clean all gates used to evaluate g in a DFS search
static void dfs_clean(nand_t *g) {
    if (g == NULL) {
        errno = EINVAL;
        return;
    }

    reset_gate(g);
    for (size_t i = 0; i < g->input_count; ++i) {
        if (g->inputs[i] == NULL) return; // we can end clean-up, as this is where the evaluation halted
        if (!g->inputs[i]->is_boolean) {
            nand_t *input = (nand_t*)(g->inputs[i]->input_signal);
            if (input->visited) dfs_clean(input);
        }
    }
    
}

// resets all gates after evaluation
static void evaluate_clean(nand_t **g, size_t m) {
    if (g == NULL) {
        errno = EINVAL;
        return;
    }
    for (size_t i = 0; i < m; ++i) {
        if (g[i] == NULL) return;
        if (g[i]->visited) dfs_clean(g[i]);
    }
}

// evaluate single gate's value and cpl
// cpl is set to -1 if evaluation fails for any reason
// errno is set to ENOMEM if it runs out of memory
// errno is set to ECANCELED if there is a loop or if NULL input is encountered at any moment
// errno is set to EINVAL if NULL or invalid structure is passed
static ssize_t evaluate_gate(bool *out_val, nand_t *gate) {
    if (out_val == NULL || gate == NULL) {
        errno = EINVAL;
        return -1;
    }

    if (gate->visited) {
        if (gate->is_on_stack) { // loop
            errno = ECANCELED;
            return -1;
        }
        *out_val = gate->result;
        return gate->cpl;
    }

    // it hasn't been evaluated
    gate->visited = true;
    gate->is_on_stack = true;
    gate->cpl = 0;
    gate->result = false;

    bool output;
    ssize_t cpl;
    const input_t *curr_input;
    for (size_t i = 0; i < gate->input_count; ++i) {
        curr_input = gate->inputs[i];
        if (curr_input == NULL) {
            errno = ECANCELED;
            return -1;
        }
        if (curr_input->is_boolean) {
            cpl = 0;
            output = *(bool*)(curr_input->input_signal);
        } else {
            cpl = evaluate_gate(&output, (nand_t*)curr_input->input_signal);
        }
        if (cpl == -1) {
            return -1;
        }
        gate->cpl = max(gate->cpl, cpl + 1);
        gate->result |= !output;
    }
    gate->is_on_stack = false;
    *out_val = gate->result;
    return gate->cpl;
}

ssize_t nand_evaluate(nand_t **g, bool *s, size_t m) {
    if (g == NULL || s == NULL || m == 0) {
        errno = EINVAL;
        return -1;
    }

    ssize_t max_cpl = 0;
    bool output;
    ssize_t cpl;
    for (size_t i = 0; i < m; ++i) {
        if (g[i] == NULL) {
            errno = EINVAL;
            evaluate_clean(g, m);
            return -1;
        }
        cpl = evaluate_gate(&output, g[i]);
        if (cpl == -1) {
            max_cpl = -1;
            break;
        }
        max_cpl = max(max_cpl, cpl);
        s[i] = output;
    }

    evaluate_clean(g, m);
    return max_cpl;
}

ssize_t nand_fan_out(nand_t const *g) {
    if (g == NULL) {
        errno = EINVAL;
        return -1;
    }
    return g->output_count;
}

void *nand_input(nand_t const *g, unsigned k) {
    if (g == NULL || k >= g->input_count) {
        errno = EINVAL;
        return NULL;
    }
    if (g->inputs[k] == NULL) {
        errno = 0;
        return NULL;
    }
    return (void*) g->inputs[k]->input_signal;
}

nand_t *nand_output(nand_t const *g, ssize_t k) {
    if (g == NULL || (size_t) k >= g->output_count) {
        errno = EINVAL;
        return NULL;
    }
    return g->outputs[k];
}
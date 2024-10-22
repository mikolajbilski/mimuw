#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

typedef struct motel {
    int net;
    int pos;
} motel;

bool are_all_different(motel* a, motel* b, motel* c) {
    return ((a->net != b->net) && (a->net != c->net) && (b->net != c->net));
}

int calculate_max_distance(motel* motels) {
    int a_to_b = motels[1].pos - motels[0].pos;
    int b_to_c = motels[2].pos - motels[1].pos;
    return a_to_b > b_to_c ? a_to_b : b_to_c;
}

void swap(motel* a, motel* b) {
    motel* t = a;
    a = b;
    b = t;
}

int calculate_min_distance(motel* a, motel* b, motel* c) {
    if (a->pos > b->pos) swap(a, b);
    if (b->pos > c->pos) swap(b, c);
    if (a->pos > b->pos) swap(a, b);
    int a_to_b = b->pos - a->pos;
    int b_to_c = c->pos - b->pos;
    return a_to_b < b_to_c ? a_to_b : b_to_c;
}

int max_all_combinations(motel* first, motel* last, motel* checked) {
    int max = 0;
    for (size_t i = 0; i < 3; ++i) {
        for (size_t j = 0; j < 3; ++j) {
            if (are_all_different(&first[i], checked, &last[j])) {
                int min = calculate_min_distance(&first[i], checked, &last[j]);
                max = max > min ? max : min;
            }
        }
    }
    return max;
}

int max_of_min(motel* motels, size_t n) {
    if (n == 0) return 0;

    int res = __INT_MAX__;
    bool exists = false;
    motel candidates[3];

    candidates[0] = motels[0];
    // we are looking for motel sequence such as ABB...BBC
    for (size_t index_a = 0; index_a < n; ++index_a) {
        if (motels[index_a].net != candidates[0].net) {
            size_t index_b = index_a;
            --index_a; //index of the "last" motel of type candidates[0]
            candidates[1] = motels[index_b];
            while (index_b < n && motels[index_b].net == candidates[1].net) ++index_b;
            if (index_b == n) break;

            candidates[2] = motels[index_b];
            if (candidates[2].net != candidates[0].net) { // we found ABB...BBC
                exists = true;
                size_t index_c = index_b;
                // calculate potential minimum
                index_b = index_a + 1;
                while (index_b < index_c) {
                    candidates[1] = motels[index_b++];
                    int bigger = calculate_max_distance(candidates);
                    res = res <= bigger ? res : bigger;
                }
                index_a = index_b - 1;
                candidates[0] = motels[index_a];
            } else { // segment ABB...BBA - skip it
                candidates[0] = motels[index_b - 1];
                index_a = index_b - 1;
            }
        } else candidates[0] = motels[index_a];
    }

    return exists ? res : 0;
}

int min_of_max(motel* motels, size_t n) { 
    if (n == 0) return 0;
    int res = 0;
    bool exists = false;
    motel first[3];
    motel last[3];
    first[0] = motels[0];

    for (size_t i = 1; i < n; ++i) {
        if (motels[i].net != first[0].net) {
            first[1] = motels[i];
            for (size_t j = i + 1; j < n; ++j) {
                if (motels[j].net != first[0].net && motels[j].net != first[1].net) {
                    first[2] = motels[j];
                    exists = true;
                    break;
                }
            }
            break;
        }
    }
    if (!exists) return 0;

    last[0] = motels[n - 1];
    for (size_t i = n - 1; i != (size_t) -1; --i) {
        if (motels[i].net != last[0].net) {
            last[1] = motels[i];
            for (size_t j = i - 1; j != (size_t) -1; --j) {
                if (motels[j].net != last[0].net && motels[j].net != last[1].net) {
                    last[2] = motels[j];
                    break;
                }
            }
            break;
        }
    }

    int curr_max = 0;
    for (size_t i = 0; i < n; ++i) {
        curr_max = max_all_combinations(first, last, &motels[i]);
        res = res > curr_max ? res : curr_max;
    }

    return res;
}

int main() {
    size_t n; scanf("%zu", &n); // 0 < n <= 1000000
    motel* motels = (motel*) malloc(n * sizeof(motel));
    for (size_t i = 0; i < n; ++i) {
        scanf("%d%d", &(motels[i].net), &(motels[i].pos));
    }
    
    int lo = max_of_min(motels, n);
    int hi = min_of_max(motels, n);
    
    printf("%d %d\n", lo, hi);

    free(motels);
    return 0;
}

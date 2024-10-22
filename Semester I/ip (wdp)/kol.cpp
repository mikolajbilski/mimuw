#include <vector>
#include <cstdlib>
#include <cstdio>

#include "kol.h"

namespace {

typedef struct {
    interesant* head;
    interesant* tail;
} kolejka;

int curr_num = 0;
std::vector<kolejka*> kolejki;

void link(interesant* el, interesant* old, interesant* nw) {
    if (el) {
        if (el->l1 == old) {
            el->l1 = nw;
        } else {
            el->l2 = nw;
        }
    }
}

void push_back(kolejka* k, interesant* a) {
    a->l1 = k->tail;
    a->l2 = k->tail->l2;
    link(k->tail->l2, k->tail, a);
    k->tail->l2 = a;
}

bool is_empty(kolejka* k) {
    return k->head->l2 == k->tail;
}

interesant* front(kolejka* k) {
    if (is_empty(k)) return NULL;
    return k->head->l2;
}

void remove_elem(interesant* a) {
    link(a->l1, a, a->l2);
    link(a->l2, a, a->l1);
}

void pop_front(kolejka* k) {
    if (is_empty(k)) return;
    remove_elem(front(k));
}

void append(kolejka &l1, kolejka &l2) {
    interesant *b = l1.tail;
    interesant *f = l2.head;
    link(b->l2, b, f->l2);
    link(f->l2, f, b->l2);
    b->l2 = f;
    f->l2 = b;
    l1.tail = l2.tail;
    l2.head = f;
    l2.tail = b;
}

interesant* next_in_queue(interesant* curr, interesant* prev) {
    if (curr == NULL) return NULL;
    return ((curr->l1 == prev) ? curr->l2 : curr->l1);
}

// checks on which side of i1 there is i2
int which_side(interesant* i1, interesant* i2) {
    interesant* a = i1->l1;
    interesant* a_prev = i1;
    interesant* a_next;
    interesant* b = i1->l2;
    interesant* b_prev = i1;
    interesant* b_next;
    while (a != i2 && b != i2) {
        if (a == NULL) return 2;
        if (b == NULL) return 1;
        a_next = next_in_queue(a, a_prev);
        b_next = next_in_queue(b, b_prev);
        a_prev = a;
        a = a_next;
        b_prev = b;
        b = b_next;
    }
    int res = (a == i2 ? 1 : 2);
    return res;
}

void reverse_queue(kolejka* k) {
    interesant* temp = k->head;
    k->head = k->tail;
    k->tail = temp;
}

kolejka* create_queue() {
    kolejka* q = (kolejka*) malloc(sizeof(kolejka));
    interesant* a = (interesant*) malloc(sizeof(interesant));
    interesant* b = (interesant*) malloc(sizeof(interesant));
    a->l1 = nullptr;
    a->l2 = b;
    b->l1 = nullptr;
    b->l2 = a;
    q->head = a;
    q->tail = b;
    return q;
}

interesant* create_interesant() {
    interesant* a = (interesant*) malloc(sizeof(interesant));
    a->num = curr_num++;
    return a;
}

} // namespace

void otwarcie_urzedu(int m) {
    for (int i = 0; i < m; ++i) kolejki.push_back(create_queue());
}

interesant *nowy_interesant(int k) {
    interesant* a = create_interesant();
    push_back(kolejki[k], a);
    return a;
}

int numerek(interesant *i) {
    return i->num;
}

interesant *obsluz(int k) {
    interesant* a = front(kolejki[k]);
    pop_front(kolejki[k]);
    return a;
}

void zmiana_okienka(interesant *i, int k) {
    remove_elem(i);
    push_back(kolejki[k], i);
}

void zamkniecie_okienka(int k1, int k2) {
    append(*kolejki[k2], *kolejki[k1]);
}

std::vector<interesant *> fast_track(interesant *i1, interesant *i2) {
    std::vector<interesant*> res = {};
    res.push_back(i1);

    if (i1 == i2) {
        remove_elem(i1);
        return res;
    }

    int side = which_side(i1, i2);
    interesant* prev = i1;
    interesant* curr;
    if (side == 1) curr = prev->l1;
    else curr = prev->l2;
    interesant* next;

    while (prev != i2) {
        res.push_back(curr);
        next = next_in_queue(curr, prev);
        remove_elem(prev);
        prev = curr;
        curr = next;
    }
    remove_elem(i2);

    return res;
}

void naczelnik(int k) {
    reverse_queue(kolejki[k]);
}

std::vector<interesant *> zamkniecie_urzedu() {
    std::vector<interesant*> res = {};

    for (int i = 0; i < (int) kolejki.size(); ++i) {
        while (!is_empty(kolejki[i])) {
            res.push_back(obsluz(i));
        }
        free(kolejki[i]->head);
        free(kolejki[i]->tail);
        free(kolejki[i]);
    }

    return res;
}
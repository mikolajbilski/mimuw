#include <math.h>
#include "ary.h"

const double EPSILON = 1e-15;

// checks if a is "equal" to 0
bool is_zero(double a) {
    return fabs(a) <= 1e-10;
}

// checks if w represents the [0;0] range
bool is_zero_range(wartosc w) {
    return (is_zero(w.lo) && is_zero(w.hi));
}

bool is_valid(wartosc w) {
    return !(isnan(w.lo) || isnan(w.hi));
}

bool contains_both_side_zero(wartosc w) {
    if (w.is_antirange) {
        return (w.lo > 0 || w.hi < 0);
    } else {
        return (w.lo < 0 && w.hi > 0);
    }
}

wartosc all_reals() {
    wartosc w;
    w.lo = -HUGE_VAL;
    w.hi = +HUGE_VAL;
    w.is_antirange = false;
    return w;
}

// "fixes" a range: "flipped" ends, antirange representing all reals and ends that are "nearly" 0
wartosc correct(wartosc w) {
    wartosc res;
    res.lo = is_zero(w.lo) ? 0 : w.lo;
    res.hi = is_zero(w.hi) ? 0 : w.hi;
    res.is_antirange = w.is_antirange;
    if (res.hi <=res.lo) {
        if (res.is_antirange) {
            return all_reals();
        }
        double t = res.lo;
        res.lo = res.hi;
        res.hi = t;
    }
    return res;
}

wartosc merge(wartosc a, wartosc b) {
    if (a.is_antirange && b.is_antirange) {
        wartosc c;
        c.lo = fmax(a.lo, b.lo);
        c.hi = fmin(a.hi, b.hi);
        c.is_antirange = true;
        return correct(c);
    }

    if ((a.is_antirange && !b.is_antirange) || (b.is_antirange && !a.is_antirange)) {
        wartosc p, q;
        if (a.is_antirange) {
            p = a;
            q = b;
        } else {
            p = b;
            q = a;
        }

        wartosc c;
        if (q.lo >= p.lo) {
            c.lo = q.hi;
            c.hi = p.hi;
        } else {
            c.lo = p.lo;
            c.hi = q.lo;
        }
        c.is_antirange = true;
        return correct(c);
    }

    if (a.hi >= b.lo) {
        wartosc c;
        c.lo = fmin(a.lo, b.lo);
        c.hi = fmax(a.hi, b.hi);
        c.is_antirange = false;
        return correct(c);
    } else {
        wartosc c;
        c.lo = fmin(a.hi, b.hi);
        c.hi = fmax(a.lo, b.lo);
        c.is_antirange = true;
        return correct(c);
    }
}

wartosc invalid_range() {
    wartosc w;
    w.lo = NAN;
    w.hi = NAN;
    w.is_antirange = false;
    return w;
}

wartosc wartosc_dokladnosc(double x, double p) {
    wartosc w;
    w.lo = x * (1 - p / 100);
    w.hi = x * (1 + p / 100);
    w.is_antirange = false;
    return correct(w);
}

wartosc wartosc_od_do(double x, double y) {
    wartosc w;
    w.lo = x;
    w.hi = y;
    w.is_antirange = false;
    return correct(w);
}

wartosc wartosc_dokladna(double x) {
    wartosc w;
    w.lo = w.hi = x;
    w.is_antirange = false;
    return correct(w);
}

bool in_wartosc(wartosc w, double x) {
    if (!is_valid(w)) return false;
    if (w.is_antirange) return (x <= w.lo + EPSILON || x >= w.hi - EPSILON);
    return (w.lo <= x + EPSILON && w.hi >= x - EPSILON);
}

double min_wartosc(wartosc w) {
    return w.is_antirange ? -HUGE_VAL : w.lo;
}

double max_wartosc(wartosc w) {
    return w.is_antirange ? +HUGE_VAL : w.hi;
}

double sr_wartosc(wartosc w) {
    if (w.is_antirange) return NAN;
    return (w.lo + w.hi) / 2;
}

wartosc plus(wartosc a, wartosc b) {
    if (!(is_valid(a) && is_valid(b))) {
        return invalid_range();
    }
    
    if (a.is_antirange && b.is_antirange) {
        return all_reals();
    }

    if ((a.is_antirange && !b.is_antirange) || (b.is_antirange && !a.is_antirange)) {
        wartosc p, q;
        if (a.is_antirange) {
            p = a;
            q = b;
        } else {
            p = b;
            q = a;
        }

        wartosc w;
        w.lo = p.lo + q.hi;
        w.hi = p.hi + q.lo;
        w.is_antirange = true;
        return correct(w);
    }

    wartosc w;
    w.lo = a.lo + b.lo;
    w.hi = a.hi + b.hi;
    w.is_antirange = false;
    return correct(w);
}

wartosc minus(wartosc a, wartosc b) {
    if (!(is_valid(a) && is_valid(b))) {
        return invalid_range();
    }

    wartosc minus_b;

    minus_b.lo = -b.hi;
    minus_b.hi = -b.lo;
    minus_b.is_antirange = b.is_antirange;

    return plus(a, minus_b);
}

wartosc razy(wartosc a, wartosc b) {
    if (!(is_valid(a) && is_valid(b))) {
        return invalid_range();
    }

    if (is_zero_range(a) || is_zero_range(b)) {
        wartosc w;
        w.lo = w.hi = 0;
        w.is_antirange = false;
        return w;
    }

    if (b.is_antirange && a.is_antirange) {
        if (in_wartosc(a, 0) || in_wartosc(b, 0)) {
            return all_reals();
        } else {
            wartosc w;
            w.lo = fmax(a.lo * b.hi, a.hi * b.lo);
            w.hi = fmin(a.lo * b.lo, a.hi * b.hi);
            w.is_antirange = true;
            return correct(w);
        }
    }

    if ((a.is_antirange && !b.is_antirange) || (b.is_antirange && !a.is_antirange)) {
        wartosc p, q;
        if (a.is_antirange) {
            p = a;
            q = b;
        } else {
            p = b;
            q = a;
        }

        if (in_wartosc(q, 0)) {
            return all_reals();
        }

        wartosc w;
        if (q.hi < 0) {
            w.lo = fmax(p.hi * q.lo, p.hi * q.hi);
            w.hi = fmin(p.lo * q.lo, p.lo * q.hi);
        } else {
            w.lo = fmax(p.lo * q.lo, p.lo * q.hi);
            w.hi = fmin(p.hi * q.lo, p.hi * q.lo);
        }
        w.is_antirange = true;
        return correct(w);
    }

    wartosc w;
    double possible_endings[4] = {a.lo * b.lo, a.lo * b.hi, a.hi * b.lo, a.hi * b.hi};
    double lo = possible_endings[0];
    double hi = possible_endings[0];
    for (int i = 1; i < 4; ++i) {
        lo = fmin(lo, possible_endings[i]);
        hi = fmax(hi, possible_endings[i]);
    }
    w.lo = lo;
    w.hi = hi;
    w.is_antirange = false;
    return correct(w);
}

wartosc podzielic(wartosc a, wartosc b) {
    if (!(is_valid(a) && is_valid(b)) || is_zero_range(b)) {
        return invalid_range();
    }

    if (is_zero_range(a)) {
        wartosc w;
        w.lo = w.hi = 0;
        w.is_antirange = false;
        return w;
    }

    if (a.is_antirange && b.is_antirange) {
        return all_reals();
    }

    if ((a.is_antirange && !b.is_antirange) || (b.is_antirange && !a.is_antirange)) {
        wartosc left_side;
        wartosc right_side;
        wartosc l_res;
        wartosc r_res;
        left_side.is_antirange = false;
        right_side.is_antirange = false;
        left_side.lo  = -HUGE_VAL;
        right_side.hi = +HUGE_VAL;
        if (a.is_antirange) {
            left_side.hi = a.lo;
            right_side.lo = a.hi;
            l_res = podzielic(left_side, b);
            r_res = podzielic(right_side, b);
        } else {
            left_side.hi = b.lo;
            right_side.lo = b.hi;
            l_res = podzielic(a, left_side);
            r_res = podzielic(a, right_side);
        }
        return merge(l_res, r_res);
    }

    if (contains_both_side_zero(b)) {
        if (in_wartosc(a, 0)) {
            return all_reals();
        } else {
            wartosc w;
            if (a.hi < 0) {
                w.lo = a.hi / b.hi;
                w.hi = a.hi / b.lo;
            } else {
                w.lo = a.lo / b.lo;
                w.hi = a.lo / b.hi;
            }
            w.is_antirange = true;
            return correct(w);
        }
    }

    wartosc inverse_b; // for b = [x;y] inverse_b = [1/y;1/x]
    if (is_zero(b.hi)) {
        inverse_b.lo = -HUGE_VAL;
    } else {
        inverse_b.lo = 1 / b.hi;
    }
    inverse_b.hi = 1 / b.lo;
    inverse_b.is_antirange = false;
    return razy(a, inverse_b);
}
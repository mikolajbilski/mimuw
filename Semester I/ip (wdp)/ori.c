#include <stdio.h>
#include <stdlib.h>
#include <math.h>

const double EPSILON = 1E-10;

typedef struct {
    double x, y;
} point;

typedef point vector;

point create_point(double x, double y) {
    point p;
    p.x = x;
    p.y = y;
    return p;
}

vector create_vector(double x, double y) {
    return create_point(x, y);
}

typedef struct {
    point p1, p2;
} rectangle;

rectangle create_rectangle(double x1, double y1, double x2, double y2) {
    rectangle r;
    r.p1 = create_point(x1, y1);
    r.p2 = create_point(x2, y2);
    return r;
}

typedef struct {
    point center;
    double r;
} circle;

circle create_circle(double x, double y, double r) {
    circle c;
    c.center = create_point(x, y);
    c.r = r;
    return c;
}

typedef struct {
    size_t k;
    point p1, p2;
} bend;

bend create_bend(size_t k, double x1, double y1, double x2, double y2) {
    bend b;
    b.k = k;
    b.p1 = create_point(x1, y1);
    b.p2 = create_point(x2, y2);
    return b;
}

typedef enum {
    RECT, CIRC, BEND
} shape_type;

typedef union {
    rectangle rect;
    circle circ;
    bend bend;
} shapes;

typedef struct {
    shape_type type;
    shapes shape;
} sheet;

// returns 1 if point p is inside rectangle r, 0 otherwise
int is_in_rectangle(rectangle r, point p) {
    return ((r.p1.x <= p.x + EPSILON && p.x <= r.p2.x + EPSILON) && (r.p1.y <= p.y + EPSILON && p.y <= r.p2.y + EPSILON));
}

// returns 1 if point p is inside circle c, 0 otherwise
int is_in_circle(circle c, point p) {
    return (pow(c.center.x - p.x, 2) + pow(c.center.y - p.y, 2) <= pow(c.r, 2) + EPSILON);
}

int is_zero(double d) {
    return fabs(d) < EPSILON;
}

// calculate the symmetry of point p relative to the line (lp1, lp2).
// if the point is to the right of the line, return -1
// if the point is on the line, return 0
// if the point is on the left of the line, return 1 and put the symmetric point into pointer symmetric
int point_symmetry(point p, point lp1, point lp2, point* symmetric) {
    // calculate the line through points lp1 and lp2
    double A = lp2.y - lp1.y;
    double B = lp1.x - lp2.x;
    double C = lp1.y * lp2.x - lp1.x * lp2.y;

    // check on which side of the line is this point
    if (is_zero(A * p.x + B * p.y + C)) return 0;
    if (A * p.x + B * p.y + C > EPSILON) return -1;
    // it is to the left: calculate symmetry

    // calculate orthogonal projection of point p on the line
    // treat both as vectors in coordinate system relavtive to lp1:
    vector line_vector = create_vector(lp2.x - lp1.x, lp2.y - lp1.y);
    vector point_vector = create_vector(p.x - lp1.x, p.y - lp1.y);

    double c = (line_vector.x * point_vector.x + line_vector.y * point_vector.y) / (pow(line_vector.x, 2) + pow(line_vector.y, 2));
    // create the projector vector
    vector proj = create_vector(c * line_vector.x, c * line_vector.y);  
    // calculate symmetry of point p relative to point represented by proj vector
    *symmetric = create_point(2 * proj.x - p.x, 2 * proj.y - p.y);
    // translate back to coordinates relative to (0, 0) from relative to lp1
    symmetric->x += 2 * lp1.x;
    symmetric->y += 2 * lp1.y;
    return 1;
}

int count_layers(sheet* sheets, size_t k, point p) {
    sheet s = sheets[k - 1];
    if (s.type == RECT) return is_in_rectangle(s.shape.rect, p);
    if (s.type == CIRC) return is_in_circle(s.shape.circ, p);
    
    point sym;
    int x = point_symmetry(p, s.shape.bend.p1, s.shape.bend.p2, &sym);
    if (x == -1) return 0;
    if (x == 0) return count_layers(sheets, s.shape.bend.k, p);
    return count_layers(sheets, s.shape.bend.k, p) + count_layers(sheets, s.shape.bend.k, sym);
}

void read_sheets(sheet* sheets, size_t n) {
    char c = ' ';
    for (size_t i = 0; i < n; ++i) {
        scanf(" %c", &c);
        double x1, y1, x2, y2;
        switch (c)
        {
        case 'P':
            sheets[i].type = RECT;
            scanf("%lf%lf%lf%lf", &x1, &y1, &x2, &y2);
            sheets[i].shape.rect = create_rectangle(x1, y1, x2, y2);
            break;
        case 'K':
            sheets[i].type = CIRC;
            double x, y, r; scanf("%lf%lf%lf", &x, &y, &r);
            sheets[i].shape.circ = create_circle(x, y, r);
            break;
        case 'Z':
            sheets[i].type = BEND;
            size_t k; scanf("%zu", &k);
            scanf("%lf%lf%lf%lf", &x1, &y1, &x2, &y2);
            sheets[i].shape.bend = create_bend(k, x1, y1, x2, y2);
            break;
        }
    }
}

int main() {
    size_t n, q; scanf("%zu%zu", &n, &q);

    sheet* sheets = (sheet*) malloc(n * sizeof(sheet));

    read_sheets(sheets, n);

    for (size_t i = 0; i < q; ++i) {
        size_t k; scanf("%zu", &k);
        double x, y; scanf("%lf%lf", &x, &y);
        point p = create_point(x, y);
        printf("%d\n", count_layers(sheets, k, p));
    }

    free(sheets);

    return 0;
}
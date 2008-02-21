//=========================================================================
//  NEDFUNCTIONS.CC - part of
//
//                    OMNeT++/OMNEST
//             Discrete System Simulation in C++
//
//=========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 1992-2005 Andras Varga

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `license' for details on this and other legal matters.
*--------------------------------------------------------------*/

#include <math.h>
#include <string.h>
#include "distrib.h"
#include "cfunction.h"
#include "cnedfunction.h"
#include "cexception.h"
#include "unitconversion.h"
#include "stringutil.h"
#include "opp_ctype.h"

USING_NAMESPACE

//TODO add "list handling" functions (like Tcl lists): lindex "SL->S", "SL->D" etc
//FIXME cDynamicExpression to add function name to exceptions thrown from functions

void nedfunctions_dummy() {} //see util.cc

typedef cDynamicExpression::Value Value;  // abbreviation for local use

#define DEF(NAME, SIGNATURE, BODY) \
    static Value NAME##_f(cComponent *context, Value argv[], int argc) {BODY} \
    Define_NED_Function2(NAME, NAME##_f, SIGNATURE);


//
// NED math.h functions
//

Define_Function(acos, 1)
Define_Function(asin, 1)
Define_Function(atan, 1)
Define_Function(atan2, 2)

Define_Function(sin, 1)
Define_Function(cos, 1)
Define_Function(tan, 1)

Define_Function(ceil, 1)
Define_Function(floor, 1)

Define_Function(exp, 1)
Define_Function(pow, 2)
Define_Function(sqrt, 1)

Define_Function(hypot, 2)

Define_Function(log, 1)
Define_Function(log10, 1)


DEF(fabs, "Q->Q", {
    argv[0].dbl = fabs(argv[0].dbl);  // preserve unit
    return argv[0];
})

DEF(fmod, "QQ->Q", {
    double argv1converted = UnitConversion::convertUnit(argv[1].dbl, argv[1].dblunit, argv[0].dblunit);
    argv[0].dbl = fmod(argv[0].dbl, argv1converted);
    return argv[0];
})

DEF(min, "QQ->Q", {
    double argv1converted = UnitConversion::convertUnit(argv[1].dbl, argv[1].dblunit, argv[0].dblunit);
    return argv[0].dbl < argv1converted ? argv[0] : argv[1];
})

DEF(max, "QQ->Q", {
    double argv1converted = UnitConversion::convertUnit(argv[1].dbl, argv[1].dblunit, argv[0].dblunit);
    return argv[0].dbl < argv1converted ? argv[1] : argv[0];
})


//
// Unit handling
//

static cStringPool stringPool;

DEF(dropunit, "Q->D", {
    argv[0].dblunit = NULL;
    return argv[0];
})

DEF(replaceunit, "QS->Q", {
    argv[0].dblunit = stringPool.get(argv[1].str.c_str());
    return argv[0];
})

DEF(convertunit, "QS->Q", {
    const char *newUnit = stringPool.get(argv[1].str.c_str());
    argv[0].dbl = UnitConversion::convertUnit(argv[0].dbl, argv[0].dblunit, newUnit);
    argv[0].dblunit = newUnit;
    return argv[0];
})

DEF(unitof, "Q->S", {
    return argv[0].dblunit;
})


//
// String manipulation functions.
//

DEF(length, "S->L", {
    return (long)argv[0].str.size();
})

DEF(contains, "SS->B", {
    return argv[0].str.find(argv[1].str) != std::string::npos;
})

DEF(substring, "SLL->S", {  //FIXME make 3rd arg optional: "SS/L->S"
    int size = argv[0].str.size();
    int index1 = (int)argv[1].dbl;
    int index2 = (int)argv[2].dbl;

    if (index1 < 0 || index1 > size)
        throw cRuntimeError("substring(): index1 out of range");
    else if (index2 < 0 || index2 > size)
        throw cRuntimeError("substring(): index2 out of range");
    return argv[0].str.substr(index1, index2 - index1);
})

DEF(startswith, "SS->B", {
    return argv[0].str.find(argv[1].str) == 0;
})

DEF(endswith, "SS->B", {
    return argv[0].str.rfind(argv[1].str) == argv[0].str.size() - argv[1].str.size();
})

DEF(tail, "SL->S", {
    int length = (int)argv[1].dbl;
    if (length < 0)
        throw cRuntimeError("tail(): length is negative");
    int size = argv[0].str.size();
    return argv[0].str.substr(max(0, size - length), size);
})

DEF(replace, "SSS->S", {
    std::string str = argv[0].str;
    std::string &search = argv[1].str;
    std::string &replacement = argv[2].str;

    int searchSize = search.size();
    int replacementSize = replacement.size();
    int index = 0;
    while ((index = str.find(search, index)) != std::string::npos) {
        str.replace(index, searchSize, replacement);
        index += replacementSize - searchSize + 1;
    }
    return str;
})

DEF(indexof, "SS->L", {
    return (long)argv[0].str.find(argv[1].str);
})

DEF(toupper, "S->S", {
    std::string tmp = argv[0].str;
    int length = tmp.length();
    for (int i=0; i<length; i++)
        tmp[i] = opp_toupper(tmp[i]);
    return tmp;
})

DEF(tolower, "S->S", {
    std::string tmp = argv[0].str;
    int length = tmp.length();
    for (int i=0; i<length; i++)
        tmp[i] = opp_tolower(tmp[i]);
    return tmp;
})

DEF(int, "*->L", {
    switch (argv[0].type) {
        case Value::BOOL:
            return argv[0].bl ? 1L : 0L;
        case Value::DBL:
            return (long)floor(argv[0].dbl);
        case Value::STR:
            return atol(argv[0].str.c_str());
        case Value::XML:
            throw cRuntimeError("int(): cannot convert xml to int");
        default:
            throw cRuntimeError("internal error: bad Value type");
    }
})

DEF(double, "*->D", {
    switch (argv[0].type) {
        case Value::BOOL:
            return argv[0].bl ? 1.0 : 0.0;
        case Value::DBL:
            return argv[0].dbl;
        case Value::STR:
            return atof(argv[0].str.c_str());
        case Value::XML:
            throw cRuntimeError("double(): cannot convert xml to double");
        default:
            throw cRuntimeError("internal error: bad Value type");
    }
})

DEF(string, "*->S", {
    return argv[0].toString();
})

//
// Random variate generation.
//

// continuous
DEF(uniform, "QQ->Q", {
    double argv1converted = UnitConversion::convertUnit(argv[1].dbl, argv[1].dblunit, argv[0].dblunit);
    argv[0].dbl = uniform(argv[0].dbl, argv1converted);
    return argv[0];
})

DEF(exponential, "Q->Q", {
    argv[0].dbl = exponential(argv[0].dbl);
    return argv[0];
})

DEF(normal, "QQ->Q", {
    double argv1converted = UnitConversion::convertUnit(argv[1].dbl, argv[1].dblunit, argv[0].dblunit);
    argv[0].dbl = normal(argv[0].dbl, argv1converted);
    return argv[0];
})

DEF(truncnormal, "QQ->Q", {
    double argv1converted = UnitConversion::convertUnit(argv[1].dbl, argv[1].dblunit, argv[0].dblunit);
    argv[0].dbl = truncnormal(argv[0].dbl, argv1converted);
    return argv[0];
})

//FIXME convert the rest!

static double _wrap_gamma_d(double alpha, double theta)
{
    return gamma_d(alpha, theta);
}

static double _wrap_beta(double alpha1, double alpha2)
{
    return beta(alpha1, alpha2);
}

static double _wrap_erlang_k(double k, double m)
{
    return erlang_k( (unsigned int) k, m);
}

static double _wrap_chi_square(double k)
{
    return chi_square( (unsigned int) k);
}

static double _wrap_student_t(double i)
{
    return student_t( (unsigned int) i);
}

static double _wrap_cauchy(double a, double b)
{
    return cauchy(a, b);
}

static double _wrap_triang(double a, double b, double c)
{
    return triang(a, b, c);
}

static double _wrap_lognormal(double m, double d)
{
    return lognormal(m, d);
}

static double _wrap_weibull(double a, double b)
{
    return weibull(a, b);
}

static double _wrap_pareto_shifted(double a, double b, double c)
{
    return pareto_shifted(a, b, c);
}

// discrete

static double _wrap_intuniform(double a, double b)
{
    return (double) intuniform((int)a, (int)b);
}

static double _wrap_bernoulli(double p)
{
    return (double) bernoulli(p);
}

static double _wrap_binomial(double n, double p)
{
    return (double) binomial( (int)n, p);
}

static double _wrap_geometric(double p)
{
    return (double) geometric(p);
}

static double _wrap_negbinomial(double n, double p)
{
    return (double) negbinomial( (int)n, p);
}

/* hypergeometric doesn't work yet
static double _wrap_hypergeometric(double a, double b, double n)
{
    return (double) hypergeometric( (int)a, (int)b, (int)n);
}
*/

static double _wrap_poisson(double lambda)
{
    return (double) poisson(lambda);
}


Define_Function2(gamma_d, _wrap_gamma_d, 2);
Define_Function2(beta, _wrap_beta, 2);
Define_Function2(erlang_k, _wrap_erlang_k, 2);
Define_Function2(chi_square, _wrap_chi_square, 1);
Define_Function2(student_t, _wrap_student_t, 1);
Define_Function2(cauchy, _wrap_cauchy, 2);
Define_Function2(triang, _wrap_triang, 3);
Define_Function2(lognormal, _wrap_lognormal, 2);
Define_Function2(weibull, _wrap_weibull, 2);
Define_Function2(pareto_shifted, _wrap_pareto_shifted, 3);

Define_Function2(intuniform, _wrap_intuniform, 2);
Define_Function2(bernoulli, _wrap_bernoulli, 1);
Define_Function2(binomial, _wrap_binomial, 2);
Define_Function2(geometric, _wrap_geometric, 1);
Define_Function2(negbinomial, _wrap_negbinomial, 2);
/* hypergeometric doesn't work yet
Define_Function2(hypergeometric, _wrap_hypergeometric, 3);
*/
Define_Function2(poisson, _wrap_poisson, 1);


// continuous, rng versions

static double _wrap_uniform_with_rng(double a, double b, double rng)
{
    return uniform(a, b, (int)rng);
}

static double _wrap_exponential_with_rng(double p, double rng)
{
    return exponential(p, (int)rng);
}

static double _wrap_normal_with_rng(double m, double d, double rng)
{
    return normal(m, d, (int)rng);
}

static double _wrap_truncnormal_with_rng(double m, double d, double rng)
{
    return truncnormal(m, d, (int)rng);
}

static double _wrap_gamma_d_with_rng(double alpha, double theta, double rng)
{
    return gamma_d(alpha, theta, (int)rng);
}

static double _wrap_beta_with_rng(double alpha1, double alpha2, double rng)
{
    return beta(alpha1, alpha2, (int)rng);
}

static double _wrap_erlang_k_with_rng(double k, double m, double rng)
{
    return erlang_k( (unsigned int) k, m, (int)rng);
}

static double _wrap_chi_square_with_rng(double k, double rng)
{
    return chi_square( (unsigned int) k, (int)rng);
}

static double _wrap_student_t_with_rng(double i, double rng)
{
    return student_t( (unsigned int) i, (int)rng);
}

static double _wrap_cauchy_with_rng(double a, double b, double rng)
{
    return cauchy(a, b, (int)rng);
}

static double _wrap_triang_with_rng(double a, double b, double c, double rng)
{
    return triang(a, b, c, (int)rng);
}

static double _wrap_lognormal_with_rng(double m, double d, double rng)
{
    return lognormal(m, d, (int)rng);
}

static double _wrap_weibull_with_rng(double a, double b, double rng)
{
    return weibull(a, b, (int)rng);
}

static double _wrap_pareto_shifted_with_rng(double a, double b, double c, double rng)
{
    return pareto_shifted(a, b, c, (int)rng);
}

// discrete, rng versions

static double _wrap_intuniform_with_rng(double a, double b, double rng)
{
    return (double) intuniform((int)a, (int)b, (int)rng);
}

static double _wrap_bernoulli_with_rng(double p, double rng)
{
    return (double) bernoulli(p, (int)rng);
}

static double _wrap_binomial_with_rng(double n, double p, double rng)
{
    return (double) binomial( (int)n, p, (int)rng);
}

static double _wrap_geometric_with_rng(double p, double rng)
{
    return (double) geometric(p, (int)rng);
}

static double _wrap_negbinomial_with_rng(double n, double p, double rng)
{
    return (double) negbinomial( (int)n, p, (int)rng);
}

/* hypergeometric doesn't work yet
static double _wrap_hypergeometric_with_rng(double a, double b, double n, double rng)
{
    return (double) hypergeometric( (int)a, (int)b, (int)n, (int)rng);
}
*/

static double _wrap_poisson_with_rng(double lambda, double rng)
{
    return (double) poisson(lambda, (int)rng);
}


Define_Function2(uniform, _wrap_uniform_with_rng, 3);
Define_Function2(exponential, _wrap_exponential_with_rng, 2);
Define_Function2(normal, _wrap_normal_with_rng, 3);
Define_Function2(truncnormal, _wrap_truncnormal_with_rng, 3);
Define_Function2(gamma_d, _wrap_gamma_d_with_rng, 3);
Define_Function2(beta, _wrap_beta_with_rng, 3);
Define_Function2(erlang_k, _wrap_erlang_k_with_rng, 3);
Define_Function2(chi_square, _wrap_chi_square_with_rng, 2);
Define_Function2(student_t, _wrap_student_t_with_rng, 2);
Define_Function2(cauchy, _wrap_cauchy_with_rng, 3);
Define_Function2(triang, _wrap_triang_with_rng, 4);
Define_Function2(lognormal, _wrap_lognormal_with_rng, 3);
Define_Function2(weibull, _wrap_weibull_with_rng, 3);
Define_Function2(pareto_shifted, _wrap_pareto_shifted_with_rng, 4);

Define_Function2(intuniform, _wrap_intuniform_with_rng, 3);
Define_Function2(bernoulli, _wrap_bernoulli_with_rng, 2);
Define_Function2(binomial, _wrap_binomial_with_rng, 3);
Define_Function2(geometric, _wrap_geometric_with_rng, 2);
Define_Function2(negbinomial, _wrap_negbinomial_with_rng, 3);
/* hypergeometric doesn't work yet
Define_Function2(hypergeometric, _wrap_hypergeometric_with_rng, 4);
*/
Define_Function2(poisson, _wrap_poisson_with_rng, 2);


//
// Meaningful error for obsolete genk_ functions
//

static double obsolete_genk_function(...)
{
    throw cRuntimeError("Obsolete function -- use the one without the \"genk_\" prefix, and rng as last argument");
}

Define_Function2(genk_uniform, (MathFunc3Args)obsolete_genk_function, 3);
Define_Function2(genk_intuniform, (MathFunc3Args)obsolete_genk_function, 3);
Define_Function2(genk_exponential, (MathFunc2Args)obsolete_genk_function, 2);
Define_Function2(genk_normal, (MathFunc3Args)obsolete_genk_function, 3);
Define_Function2(genk_truncnormal, (MathFunc3Args)obsolete_genk_function, 3);


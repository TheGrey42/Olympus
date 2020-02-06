#!/usr/bin/python
# Author: Dawson Godby
# Date: 09 Sep 2019
# Language: Python 3.7
# Assignment: SAT
import random
import time

def GettingInput():
    UserInput = input("Which File would you like to import?: ")
    return UserInput

def FitnessTest(TestString, TestList):
    Fitness = 0
    # Loops through each sub expression
    for i in range(len(TestList)):
        # Resets the flag for each expression
        HasFitnessIncremented = False
        for j in range(len(TestList[i])):
            # Looks at each expression if the condition has not been meet
            if not HasFitnessIncremented:
                # Looks at each variable in expression and checks if string meets condition
                for k in range(len(TestString)):
                    if (TestString[k] == TestList[i][j]) and not HasFitnessIncremented:
                        Fitness = Fitness + 1
                        HasFitnessIncremented = True

    return Fitness


def ExtractFile(filename):
    statements = []
    parameters = []
    # Opens users desired file
    testFile = open(filename, "r")
    if testFile.mode == 'r':
        # Reads File into List
        contents = testFile.readlines()
        for i in contents:
            # Removes Comments
            if i[0] == 'c':
                # Nothing
                worthless = 0
            # Processes the parameters
            elif (i[0] == 'p'):
                parameters = i.split(" ")
            # Handles End of File
            elif (i[0] == '%'):
                break
            # Passes statements to next list
            else:
                i = i.strip('\n')
                if i != '0':
                    statements.append(i)
    else:
        return False
    FitnessList = []
    # Breaks the list into a 2D list by the spaces present
    for i in statements:
        i = i.split(' ')
        if i != '':
            FitnessList.append(i)
    FinalList = []
    # Converts the strings into integers and then removes any empty string
    for i in FitnessList:
        transferList = []
        for j in i:
            if j != '':
                test = int(j)
                if test != 0:
                    transferList.append(test)
        FinalList.append(transferList)
    Results = [FinalList, parameters[2], parameters[3]]
    return Results


# Simplifies expression given a literal
def Simplify(expression, literal):
    negLit = literal * -1
    # Removes instances of -Literal
    for i in expression:
        if negLit in i:
            i.remove(negLit)
    ClausesToDelete = []
    for Clause in expression:
        if literal in Clause:
            ClausesToDelete.append(Clause)
    NewExpression = expression.copy()
    for Clause in ClausesToDelete:
        if Clause in expression:
            NewExpression.remove(Clause)
    return NewExpression


def PureSimplify(expression, literal):
    ClausesToDelete = []
    for Clause in expression:
        if literal in Clause:
            ClausesToDelete.append(Clause)
    NewExpression = expression.copy()
    for Clause in ClausesToDelete:
        if Clause in expression:
            NewExpression.remove(Clause)
    return NewExpression


def DPLL(expression):
    # If expression is empty is satisfied
    if len(expression) == 0:
        return True
    # if there is an empty clause it is unsatisfied
    for Clauses in expression:
        if len(Clauses) == 0:
            return False
    Flag = True
    # Loops while there are unit clauses to simplify
    while Flag:
        Flag = False
        # Collects all the unit clauses
        UnitClauses = []
        for Clauses in expression:
            if len(Clauses) == 1:
                Flag = True
                Literal = Clauses[0]
                UnitClauses.append(Literal)
        # Reduces the expression
        for Literal in UnitClauses:
            expression = Simplify(expression, Literal)
        # If any literals found loops again
        if len(UnitClauses) != 0:
            Flag = True

    # If there is an empty clause it is unsatisfiable
    for Clauses in expression:
        if len(Clauses) == 0:
            return False

    # Loops till no new Pure Literals
    Flag = True
    while Flag:
        Flag = False
        # Adds all literals to list
        PureLiterals = []
        AllLiterals = []
        for Clause in expression:
            for Literal in Clause:
                if not(Literal in AllLiterals):
                    AllLiterals.append(Literal)

        # Adds literals to list of Pure Literals if their negative counterpart isnt present
        for Literal in AllLiterals:
            NegativeLiteral = Literal * -1
            if not((Literal in AllLiterals) and (NegativeLiteral in AllLiterals)):
                PureLiterals.append(Literal)
        # Removes Clauses that posses a Pure Literal
        for Literal in PureLiterals:
            expression = PureSimplify(expression, Literal)
        # Loops till no
        if len(PureLiterals) != 0:
            Flag = True

    # If there are no more clauses then it is Satisfied
    if len(expression) == 0:
        return True

    # Tries each Literal in expression to reccursively call and Solve DPLL
    for Clause in expression:
        for Literal in Clause:
            PositiveLiteral = abs(Literal)
            NegativeLiteral = abs(Literal) * -1
            TrueDPLL = expression.copy()
            FalseDPLL = expression.copy()
            TrueDPLL = Simplify(TrueDPLL, PositiveLiteral)
            FalseDPLL = Simplify(FalseDPLL, NegativeLiteral)
            Trial = DPLL(TrueDPLL)
            OtherTrial = DPLL(FalseDPLL)
            if Trial or OtherTrial:
                return True
    return False


# Helper Function for WalkSatFA  that sees if a string satisfies a aingle Clause
def DoesSatisfy(AnswerString, clause):
    Satisfied = False
    for i in range(len(clause)):
        for j in range(len(AnswerString)):
            if clause[i] == AnswerString[j]:
                Satisfied = True
    return Satisfied


# Helper Function for WalkSat that returns indexes of all the unsolved clauses
def WalkSatFA(expression, AnswerString):
    UnsolvedIndexes = []
    for i in range(len(expression)):
        if not (DoesSatisfy(AnswerString, expression[i])):
            UnsolvedIndexes.append(i)
    return UnsolvedIndexes


def WalkSat(expression, parameter):
    # The maximun number of clauses it can satisfy
    MaxFitness = len(expression)
    BestSoFar = []
    # Since it is a probability heuristic it runs 10 times and returns the best one
    for m in range(10):
        # Makes a random string of the literals
        AnswerString = []
        for i in range(parameter):
            if random.random() > .5:
                AnswerString.append((i + 1) * -1)
            else:
                AnswerString.append(i + 1)
        MaxFlips = 100
        # Sets the maximum number of flips to 100
        for i in range(MaxFlips):
            # Evaluates the Fitness of the current string and returns it if it is at the maximum
            Fitness = FitnessTest(AnswerString, expression)
            if Fitness == MaxFitness:
                return AnswerString
            # Calls WALKSATFA to find a list of clauses that are unsatisfied
            indexes = WalkSatFA(expression, AnswerString)
            # Chooses an index from that list
            choiceIndex = random.randrange(len(indexes))
            index = indexes[choiceIndex]
            clause = expression[index].copy()
            # 50% of the time it will flip a random variable from that clause
            if random.random() > .5:
                Index2 = random.randrange(len(expression[index]))
                value = clause[Index2]
                Index3 = abs(value) - 1
                AnswerString[Index3] = AnswerString[Index3] * -1
            # The other 50% of the time flips the variable in that clause that causes the best increase in fitness
            else:
                BestIndex = -1
                BestFitness = -1
                # Goes through each of the variables in that clause and selects the best one to flip
                for j in range(len(clause)):
                    NewString = AnswerString.copy()
                    ValueToCheck = clause[j]
                    StringIndex = abs(ValueToCheck) - 1
                    # Finds the Fitness of each of the variable changes
                    NewString[StringIndex] = NewString[StringIndex] * -1
                    NewFitness = FitnessTest(NewString, expression)
                    # If Fitness of current string is better than the best one
                    # It becomes the best string
                    if NewFitness > BestFitness:
                        BestIndex = StringIndex
                        BestFitness = NewFitness
                # Flips the Variable that has the best fitness
                AnswerString[BestIndex] = AnswerString[BestIndex] * -1
        # Updates the Best String So Far if the current answer is better than any of the others
        if FitnessTest(AnswerString, expression) > FitnessTest(BestSoFar, expression):
            BestSoFar = AnswerString.copy()
    return BestSoFar


def GSAT(expression, parameter):
    # Max Number of Clauses it can satisfy
    MaxFitness = len(expression)
    BestSoFar = []
    # Since it is a probability based it runs 10 times and collects the best string out of those
    for m in range(10):
        AnswerString = []
        # Generates Random
        for i in range(parameter):
            if random.random() > .5:
                AnswerString.append((i + 1) * -1)
            else:
                AnswerString.append(i + 1)
        Fitness = FitnessTest(AnswerString, expression)
        MaxFlips = 100
        # Sets the Maximum number of Flips to 100
        for i in range(MaxFlips):
            # Returns if solution is found
            if Fitness == MaxFitness:
                return AnswerString
            # Has a 50% chance of selecting to flip Literal that satusfies the most clauses
            if random.random() < .5:
                BestIndex = -1
                BestFitness = -1
                for j in range(len(AnswerString)):
                    # Creates copy of current String
                    NewString = AnswerString.copy()
                    ValueToCheck = AnswerString[j]
                    StringIndex = abs(ValueToCheck) - 1
                    # Flips the value of that String
                    NewString[StringIndex] = NewString[StringIndex] * -1
                    # finds the Fitness of that new string
                    NewFitness = FitnessTest(NewString, expression)
                    # If the fitness is better than any other it becomes the new best
                    if NewFitness > BestFitness:
                        BestIndex = StringIndex
                        BestFitness = NewFitness
                # Changes AnswerString to that value
                AnswerString[BestIndex] = AnswerString[BestIndex] * -1
            # Otherwise flips random bit
            else:
                index = random.randrange(len(AnswerString))
                AnswerString[index] = AnswerString[index] * -1
        # Takes the best answer of the 10 and returns it
        if FitnessTest(AnswerString, expression) > FitnessTest(BestSoFar, expression):
            BestSoFar = AnswerString.copy()
    return BestSoFar

# Tests how many clauses the answer from a heuristic meets
def RunStatistics(expression, AnswerString):
    MaxFitness = len(expression)
    Fitness = FitnessTest(AnswerString, expression)
    print("MaxFitness is", MaxFitness)
    print("Fitness of expression is", Fitness)


def main():
    filename = GettingInput()
    # filename = "C:/Users/dgodb/PycharmProjects/DPLLSolve/Hard CNF Formulas/100.410.699718290.cnf"
    print(filename)
    Results = ExtractFile(filename)
    if not Results:
        print("Failure to find file please rerun and check your syntax")
        return -42
    # Expression to Test
    TestExpressions = Results[0]
    WalkCopy = []
    GSATcopy = []
    DPLLcopy = []
    for i in range(len(TestExpressions)):
        WalkCopy.append(TestExpressions[i].copy())
        GSATcopy.append(TestExpressions[i].copy())
        DPLLcopy.append(TestExpressions[i].copy())
    # Sets the range of variables that can occur
    ExpressionRange = int(Results[1])
    # Starts DPLL Test and Times it
    TimeInitial = time.time()
    IsSatisfied = DPLL(DPLLcopy)
    TimeEnd = time.time()
    print("Is this satisfiable?:", IsSatisfied)
    print("Time to Complete:", TimeEnd-TimeInitial)
    Walk = WalkSat(WalkCopy, ExpressionRange)
    TimeEnd = time.time()
    RunStatistics(WalkCopy, Walk)
    print("WalkSAT:", Walk)
    print("Time to Complete:", TimeEnd - TimeInitial)
    TimeInitial = time.time()
    GSATanswer = GSAT(GSATcopy, ExpressionRange)
    TimeEnd = time.time()
    RunStatistics(GSATcopy, GSATanswer)
    print("GSAT:", GSATanswer)
    print("Time to complete:", TimeEnd - TimeInitial)


main()
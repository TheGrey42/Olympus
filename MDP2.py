#!/usr/bin/python

# Global Variable that is an array of all the coordinates states in the 4x4 MDP
states = [[0, 0], [0, 1], [0, 2], [0, 3],
          [1, 0], [1, 1], [1, 2], [1, 3],
          [2, 0], [2, 1], [2, 2], [2, 3],
          [3, 0], [3, 1], [3, 2], [3, 3]]


# currentstate is an array for which the first descriptor is the array that stores the
# coordinates and the second descriptor is the value at the coordinate

# current state is an array containing coordinates at 0 and its reward value at 1
def MaxExpectedValue(currentState, MDP):
    RewardStay = currentState[1]
    RewardUp = 0
    RewardDown = 0
    RewardLeft = 0
    RewardRight = 0
    # Flags if there is no direction for it to check
    currentCoordinates = currentState[0].copy()
    if currentCoordinates[0] == 0:
        RewardUp = -4242
    if currentCoordinates[0] == 3:
        RewardDown = -4242
    if currentCoordinates[1] == 0:
        RewardLeft = -4242
    if currentCoordinates[1] == 3:
        RewardRight = -4242

    # Updates the values of the rewards for each
    if RewardUp != -4242:
        RewardUp = int(MDP[currentCoordinates[0] - 1][currentCoordinates[1]])
    # Assigns current value for a direction if it would bump a wall
    else:
        RewardUp = RewardStay
    if RewardDown != -4242:
        RewardDown = MDP[currentCoordinates[0] + 1][currentCoordinates[1]]
    else:
        RewardDown = RewardStay
    if RewardLeft != -4242:
        RewardLeft = MDP[currentCoordinates[0]][currentCoordinates[1] - 1]
    else:
        RewardLeft = RewardStay
    if RewardRight != -4242:
        RewardRight = MDP[currentCoordinates[0]][currentCoordinates[1] + 1]
    else:
        RewardRight = RewardStay

    # Calculates the Expected Value at for heading in each direction
    EVUP = (RewardUp * .7) + (RewardStay * .1) + (RewardDown * .2)
    EVDOWN = (RewardDown * .7) + (RewardStay * .1) + (RewardUp * .2)
    EVLEFT = (RewardLeft * .7) + (RewardStay * .1) + (RewardRight * .2)
    EVRIGHT = (RewardRight * .7) + (RewardStay * .1) + (RewardLeft * .2)

    # Statements to see which direction has the greatest Expected Value
    if (EVUP > EVDOWN) and (EVUP > EVLEFT) and (EVUP > EVRIGHT) and (EVUP > RewardStay):
        return EVUP
    elif (EVDOWN > EVUP) and (EVDOWN > EVLEFT) and (EVDOWN > EVRIGHT) and (EVDOWN > RewardStay):
        return EVDOWN
    elif (EVLEFT > EVDOWN) and (EVLEFT > EVUP) and (EVLEFT > EVRIGHT) and (EVLEFT > RewardStay):
        return EVLEFT
    elif (EVRIGHT > EVDOWN) and (EVRIGHT > EVLEFT) and (EVRIGHT > EVUP) and (EVRIGHT > RewardStay):
        return EVRIGHT
    else:
        return RewardStay


# Same as MaxExpectedValue but for Infinite Horizon with discount
def MaxExpectedValueInfinite(currentState, MDP, gamma):
    RewardStay = currentState[1]
    RewardUp = 0
    RewardDown = 0
    RewardLeft = 0
    RewardRight = 0
    # Flags if it cant head in that direction
    currentCoordinates = currentState[0].copy()
    if currentCoordinates[0] == 0:
        RewardUp = -4242
    if currentCoordinates[0] == 3:
        RewardDown = -4242
    if currentCoordinates[1] == 0:
        RewardLeft = -4242
    if currentCoordinates[1] == 3:
        RewardRight = -4242

    # Updates the values of the rewards for each
    if RewardUp != -4242:
        RewardUp = int(MDP[currentCoordinates[0] - 1][currentCoordinates[1]])
    # Otherwise sets its reward to its current state
    else:
        RewardUp = RewardStay
    if RewardDown != -4242:
        RewardDown = MDP[currentCoordinates[0] + 1][currentCoordinates[1]]
    else:
        RewardDown = RewardStay
    if RewardLeft != -4242:
        RewardLeft = MDP[currentCoordinates[0]][currentCoordinates[1] - 1]
    else:
        RewardLeft = RewardStay
    if RewardRight != -4242:
        RewardRight = MDP[currentCoordinates[0]][currentCoordinates[1] + 1]
    else:
        RewardRight = RewardStay

    # Calculates the Expected Value at for heading in each direction
    EVUP = (RewardUp * .7) + (RewardStay * .1) + (RewardDown * .2)
    EVDOWN = (RewardDown * .7) + (RewardStay * .1) + (RewardUp * .2)
    EVLEFT = (RewardLeft * .7) + (RewardStay * .1) + (RewardRight * .2)
    EVRIGHT = (RewardRight * .7) + (RewardStay * .1) + (RewardLeft * .2)

    # Statements to see which direction has the greatest Expected Value
    if (EVUP > EVDOWN) and (EVUP > EVLEFT) and (EVUP > EVRIGHT) and (EVUP > RewardStay):
        return EVUP * gamma
    elif (EVDOWN > EVUP) and (EVDOWN > EVLEFT) and (EVDOWN > EVRIGHT) and (EVDOWN > RewardStay):
        return EVDOWN * gamma
    elif (EVLEFT > EVDOWN) and (EVLEFT > EVUP) and (EVLEFT > EVRIGHT) and (EVLEFT > RewardStay):
        return EVLEFT * gamma
    elif (EVRIGHT > EVDOWN) and (EVRIGHT > EVLEFT) and (EVRIGHT > EVUP) and (EVRIGHT > RewardStay):
        return EVRIGHT * gamma
    else:
        return RewardStay


# Gets the information of reward and coordinates and returns them as an array
def getState(coordinates, MDP):
    currentState = []
    currentState.append(coordinates)
    currentState.append(MDP[coordinates[0]][coordinates[1]])
    return currentState


# Prettifies printing a 2D array
def ArrayPrint2D(needsPrint):
    for i in needsPrint:
        print(i)
    print()


#Failure of a function
def PolicyGenerator(MDP):
    policy = MDP.copy()
    for state in states:
        direction = findHeading(state, MDP)
        policy[state[0]][state[1]] = direction
    ArrayPrint2D(policy)


# Failure of a function
def findHeading(state, MDP):
    if state[0] == 0:
        RewardUp = -4242
    else:
        RewardUp = MDP[state[0] -1][state[1]]
    if state[0] == 3:
        RewardDown = -4242
    else:
        RewardDown = MDP[state[0] +1][state[1]]
    if state[1] == 0:
        RewardLeft = -4242
    else:
        RewardLeft = MDP[state[0]][state[1] -1]
    if state[1] == 3:
        RewardRight = -4242
    else:
        RewardRight = MDP[state[0]][state[1] +1]
    RewardStay = MDP[state[0]][state[1]]

    if (RewardUp > RewardDown) and (RewardUp > RewardLeft) and (RewardUp > RewardRight) and (RewardUp > RewardStay):
        return "^"
    if (RewardDown > RewardUp) and (RewardDown > RewardLeft)and(RewardDown > RewardRight) and (RewardDown > RewardStay):
        return "v"
    if (RewardLeft > RewardRight) and (RewardLeft > RewardUp) and (RewardLeft > RewardDown) and (RewardLeft > RewardStay):
        return "<"
    if (RewardRight > RewardLeft) and (RewardRight > RewardDown) and (RewardRight > RewardUp) and (RewardRight > RewardStay):
        return ">"
    if (RewardStay >= RewardUp) and (RewardStay >= RewardDown) and (RewardStay >= RewardLeft) and (RewardStay >= RewardRight):
        return "."


# Checks if the change from the last update of the MDP meets the Bellman Residue Clause
def BellmanResidueCheck(prechange, postchange, BellmanResidue):
    for state in  states:
        check1 = prechange[state[0]][state[1]]
        check2 = postchange[state[0]][state[1]]
        difference = abs(check1 - check2)
        # Returns if the difference is greater than the Bellman Residue at any point
        if difference > BellmanResidue:
            return True
    # If it makes it here there is no state with a difference greater than the Bellman Residue
    return False


def ValueIterationFiniteHorizon(MDP, horizon):
    dynamicMDP = MDP.copy()
    for k in range(horizon):
        for state in states:
            currentState = getState(state, dynamicMDP)
            value = MaxExpectedValue(currentState, dynamicMDP)
            dynamicMDP[state[0]][state[1]] = value
    ArrayPrint2D(dynamicMDP)


def ValueIterationInfiniteHorizon(MDP):
    dynamicMDP = MDP.copy()
    Gamma = .96
    BellmanResidue = .005
    Flag = True
    while Flag:
        for state in states:
            currentState = getState(state, dynamicMDP)
            prechange = dynamicMDP.copy()
            value = MaxExpectedValueInfinite(currentState, MDP, Gamma)
            dynamicMDP[state[0]][state[1]] = value
        Flag = BellmanResidueCheck(prechange, dynamicMDP, BellmanResidue)
    ArrayPrint2D(dynamicMDP)


def main():
    MDP = [
        [0, 5, -2, 10],
        [0, 5, 0, 15],
        [-5, 10, 5, 0],
        [60, 0, 0, 5]
    ]
    print("Original:")
    ArrayPrint2D(MDP)
    print("Finite Horizon:")
    ValueIterationFiniteHorizon(MDP, 6)
    print("Infinite Horizon:")
    ValueIterationInfiniteHorizon(MDP)


main()

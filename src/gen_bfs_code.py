RADIUS_SQUARED = 20
OUTPUT_FILENAME = "bfs_code.txt"
start_loc = (5, 4)  # you may need to increase these vals if the RADIUS_SQUARED is increased from 20


def get_neighbor_locs(loc, order=None):
    neighbors = []
    dirs = [(-1, -1), (-1, 0), (-1, 1), (0, 1), (1, 1), (1, 0), (1, -1), (0, -1)]
    for dir in dirs:
        neighbors.append((loc[0]+dir[0], loc[1]+dir[1]))

    if order != None:
        valid_neighbors = []
        for neighbor in neighbors:
            if neighbor in order:
                valid_neighbors.append(neighbor)
        return valid_neighbors

    return neighbors


def get_edge_locs(order):
    # find spots that are on the edge of our circle (for last segment in code generation)
    edge_locs = []
    for loc in order:
        neighbors = get_neighbor_locs(loc, order)
        if len(neighbors) != 8:
            edge_locs.append(loc)
    return edge_locs


move_arr = []
for i in range(0, RADIUS_SQUARED):
    for j in range(0, i+1):
        if i**2 + j**2 <= RADIUS_SQUARED:
            move_arr.append((i, j))
            if i != j:
                move_arr.append((j, i))
        else:
            break


# order in which we will conduct updates to heuristic
order = []
diff = []
for dx, dy in move_arr:
    for x_dir in [-1, 1]:
        for y_dir in [-1, 1]:
            x_move, y_move = x_dir*dx, y_dir*dy
            new_loc = (start_loc[0]+x_move, start_loc[1]+y_move)
            if new_loc not in order:
                order.append(new_loc)
                diff.append((x_move, y_move))


dir_dict = {(-1, -1): "NORTHWEST",
            (-1, 0): "NORTH",
            (-1, 1): "NORTHEAST",
            (0, 1): "EAST",
            (1, 1): "SOUTHEAST",
            (1, 0): "SOUTH",
            (1, -1): "SOUTHWEST",
            (0, -1): "WEST"}

opp_dir_dict = {(x, y): dir_dict[(-1*x, -1*y)] for (x, y) in dir_dict.keys()}

# generating directions to create locations
direction_arr = [(start_loc, None)]
for i in range(1, len(order)):
    curr_x, curr_y = order[i]
    dirs = [(-1, -1), (-1, 0), (-1, 1), (0, 1), (1, 1), (1, 0), (1, -1), (0, -1)]
    for dir in dirs:
        prev_loc = (curr_x+dir[0], curr_y+dir[1])
        if prev_loc in order[0:i]:
            direction_arr.append((order[i], prev_loc, opp_dir_dict[dir]))


# generating diff_dict for try_catch code
diff_dict = {x: [] for x in set([i for (i, j) in diff])}
for d in diff:
    diff_dict[d[0]].append(d[1])

for key in diff_dict.keys():
    diff_dict[key].sort()

######################################################################################################
# CODE GENERATION
f = open(OUTPUT_FILENAME, "w")

f.write("import battlecode.common.*;\n")
f.write("public class BFS{}{{\n".format(RADIUS_SQUARED))
f.write("RobotController rc;\nRobot robot;\n")

f.write("BFS{}(RobotController rc, Robot robot){{\n".format(RADIUS_SQUARED))
f.write("this.rc = rc;\nthis.robot=robot;\n}\n")

# section 1
for x, y in order:
    f.write("static MapLocation l{}{};\n".format(x, y))
    f.write("static double v{}{};\n".format(x, y))
    f.write("static Direction d{}{};\n".format(x, y))
    f.write("static double p{}{};\n".format(x, y))
    f.write("\n"*2)


# section 2
f.write("Direction getBestDir(MapLocation target) throws GameActionException{\n")

f.write("l{}{} = robot.myLoc;\n".format(start_loc[0], start_loc[1]))
f.write("v{}{} = 0;\n\n".format(start_loc[0], start_loc[1]))

for d in direction_arr[1:]:
    curr_x, curr_y = d[0]
    prev_x, prev_y = d[1]
    dir = d[2]
    f.write("l{}{} = l{}{}.add(Direction.{});\n".format(curr_x, curr_y, prev_x, prev_y, dir))
    f.write("v{}{} = 100000000;\n".format(curr_x, curr_y, prev_x, prev_y, dir))
    f.write("d{}{} = null;\n\n".format(curr_x, curr_y))


# section 3
f.write("try{ ")
f.write("double sum;")
for i in range(1, len(order)):
    x, y = order[i]
    f.write("if(rc.onTheMap(l{}{})){{\n".format(x, y))
    if start_loc in get_neighbor_locs((x, y)):
        f.write("if(!rc.isLocationOccupied(l{}{})){{\n".format(x, y))
    f.write("p{}{} = 20*(rc.senseRubble(l{}{})/10 + 1);\n".format(x, y, x, y))

    for neighbor in get_neighbor_locs((x, y)):
        if neighbor in order[0:i]:        # check if we have updated this neighbor's value already
            n_x, n_y = neighbor
            f.write("sum = v{}{} + p{}{};\n".format(n_x, n_y, x, y))
            f.write("if(v{}{} > sum){{\n".format(x, y))
            f.write("v{}{} = sum;\n".format(x, y))

            if (n_x, n_y) == start_loc:
                x_incr, y_incr = x-n_x, y-n_y
                direction = dir_dict[(x_incr, y_incr)]
                f.write("d{}{} = Direction.{};\n".format(x, y, direction))
            else:
                f.write("d{}{} = d{}{};\n".format(x, y, n_x, n_y))
            f.write("}\n")

    if start_loc in get_neighbor_locs((x, y)):
        f.write("}\n")
    f.write("}\n")


# section 4 (try catch section to check if we have just explored the target)
f.write("int dx = target.x - l{}{}.x;\n".format(start_loc[0], start_loc[1]))
f.write("int dy = target.x - l{}{}.y;\n".format(start_loc[0], start_loc[1]))

f.write("switch (dx) {\n")
for dx in sorted(list(diff_dict.keys())):
    f.write("case {}:\n".format(dx))
    f.write("switch (dy){\n")
    for dy in diff_dict[dx]:
        target_x, target_y = dx+start_loc[0], dy+start_loc[1]
        f.write("case {}:\n".format(dy))
        f.write("return d{}{};\n".format(target_x, target_y))
    f.write("}\n")
    f.write("break;\n")
f.write("}\n")


f.write("Direction ans = null;\n")
f.write("double bestScore = 0;\n")
f.write("double currScore;\n")

edge_locs = get_edge_locs(order)
for edge_loc in edge_locs:
    e_x, e_y = edge_loc
    f.write("currScore = l{}{}.distanceSquaredTo(target) + v{}{}/10;\n".format(e_x, e_y, e_x, e_y))
    f.write("if(currScore < bestScore){\n")
    f.write("bestScore = currScore;\n")
    f.write("ans = d{}{};\n".format(e_x, e_y))
    f.write("}\n")

f.write("return ans;\n")
f.write("} catch (Exception e){\n")
f.write("e.printStackTrace();\n")
f.write("}")
f.write("return null;\n")
f.write("}\n")
f.write("}\n")
# f.write("initialDist = Math.sqrt(l{}{})")

# print(order)
# print((5, 4) in order)

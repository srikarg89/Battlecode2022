package sprintbot;
import battlecode.common.*;
public class BFS20{
    RobotController rc;
    Robot robot;
    BFS20(RobotController rc, Robot robot){
        this.rc = rc;
        this.robot=robot;
    }
    static MapLocation l54;
    static double v54;
    static Direction d54;
    static double p54;


    static MapLocation l44;
    static double v44;
    static Direction d44;
    static double p44;


    static MapLocation l64;
    static double v64;
    static Direction d64;
    static double p64;


    static MapLocation l53;
    static double v53;
    static Direction d53;
    static double p53;


    static MapLocation l55;
    static double v55;
    static Direction d55;
    static double p55;


    static MapLocation l43;
    static double v43;
    static Direction d43;
    static double p43;


    static MapLocation l45;
    static double v45;
    static Direction d45;
    static double p45;


    static MapLocation l63;
    static double v63;
    static Direction d63;
    static double p63;


    static MapLocation l65;
    static double v65;
    static Direction d65;
    static double p65;


    static MapLocation l34;
    static double v34;
    static Direction d34;
    static double p34;


    static MapLocation l74;
    static double v74;
    static Direction d74;
    static double p74;


    static MapLocation l52;
    static double v52;
    static Direction d52;
    static double p52;


    static MapLocation l56;
    static double v56;
    static Direction d56;
    static double p56;


    static MapLocation l33;
    static double v33;
    static Direction d33;
    static double p33;


    static MapLocation l35;
    static double v35;
    static Direction d35;
    static double p35;


    static MapLocation l73;
    static double v73;
    static Direction d73;
    static double p73;


    static MapLocation l75;
    static double v75;
    static Direction d75;
    static double p75;


    static MapLocation l42;
    static double v42;
    static Direction d42;
    static double p42;


    static MapLocation l46;
    static double v46;
    static Direction d46;
    static double p46;


    static MapLocation l62;
    static double v62;
    static Direction d62;
    static double p62;


    static MapLocation l66;
    static double v66;
    static Direction d66;
    static double p66;


    static MapLocation l32;
    static double v32;
    static Direction d32;
    static double p32;


    static MapLocation l36;
    static double v36;
    static Direction d36;
    static double p36;


    static MapLocation l72;
    static double v72;
    static Direction d72;
    static double p72;


    static MapLocation l76;
    static double v76;
    static Direction d76;
    static double p76;


    static MapLocation l24;
    static double v24;
    static Direction d24;
    static double p24;


    static MapLocation l84;
    static double v84;
    static Direction d84;
    static double p84;


    static MapLocation l51;
    static double v51;
    static Direction d51;
    static double p51;


    static MapLocation l57;
    static double v57;
    static Direction d57;
    static double p57;


    static MapLocation l23;
    static double v23;
    static Direction d23;
    static double p23;


    static MapLocation l25;
    static double v25;
    static Direction d25;
    static double p25;


    static MapLocation l83;
    static double v83;
    static Direction d83;
    static double p83;


    static MapLocation l85;
    static double v85;
    static Direction d85;
    static double p85;


    static MapLocation l41;
    static double v41;
    static Direction d41;
    static double p41;


    static MapLocation l47;
    static double v47;
    static Direction d47;
    static double p47;


    static MapLocation l61;
    static double v61;
    static Direction d61;
    static double p61;


    static MapLocation l67;
    static double v67;
    static Direction d67;
    static double p67;


    static MapLocation l22;
    static double v22;
    static Direction d22;
    static double p22;


    static MapLocation l26;
    static double v26;
    static Direction d26;
    static double p26;


    static MapLocation l82;
    static double v82;
    static Direction d82;
    static double p82;


    static MapLocation l86;
    static double v86;
    static Direction d86;
    static double p86;


    static MapLocation l31;
    static double v31;
    static Direction d31;
    static double p31;


    static MapLocation l37;
    static double v37;
    static Direction d37;
    static double p37;


    static MapLocation l71;
    static double v71;
    static Direction d71;
    static double p71;


    static MapLocation l77;
    static double v77;
    static Direction d77;
    static double p77;


    static MapLocation l21;
    static double v21;
    static Direction d21;
    static double p21;


    static MapLocation l27;
    static double v27;
    static Direction d27;
    static double p27;


    static MapLocation l81;
    static double v81;
    static Direction d81;
    static double p81;


    static MapLocation l87;
    static double v87;
    static Direction d87;
    static double p87;


    static MapLocation l14;
    static double v14;
    static Direction d14;
    static double p14;


    static MapLocation l94;
    static double v94;
    static Direction d94;
    static double p94;


    static MapLocation l50;
    static double v50;
    static Direction d50;
    static double p50;


    static MapLocation l58;
    static double v58;
    static Direction d58;
    static double p58;


    static MapLocation l13;
    static double v13;
    static Direction d13;
    static double p13;


    static MapLocation l15;
    static double v15;
    static Direction d15;
    static double p15;


    static MapLocation l93;
    static double v93;
    static Direction d93;
    static double p93;


    static MapLocation l95;
    static double v95;
    static Direction d95;
    static double p95;


    static MapLocation l40;
    static double v40;
    static Direction d40;
    static double p40;


    static MapLocation l48;
    static double v48;
    static Direction d48;
    static double p48;


    static MapLocation l60;
    static double v60;
    static Direction d60;
    static double p60;


    static MapLocation l68;
    static double v68;
    static Direction d68;
    static double p68;


    static MapLocation l12;
    static double v12;
    static Direction d12;
    static double p12;


    static MapLocation l16;
    static double v16;
    static Direction d16;
    static double p16;


    static MapLocation l92;
    static double v92;
    static Direction d92;
    static double p92;


    static MapLocation l96;
    static double v96;
    static Direction d96;
    static double p96;


    static MapLocation l30;
    static double v30;
    static Direction d30;
    static double p30;


    static MapLocation l38;
    static double v38;
    static Direction d38;
    static double p38;


    static MapLocation l70;
    static double v70;
    static Direction d70;
    static double p70;


    static MapLocation l78;
    static double v78;
    static Direction d78;
    static double p78;


    Direction runBFS(MapLocation target) throws GameActionException{
        System.out.println("Starting BFS Method: " + Clock.getBytecodesLeft());
        l54 = robot.myLoc;
        v54 = 0;

        l44 = l54.add(Direction.WEST);
        v44 = 100000000;
        d44 = null;

        l64 = l54.add(Direction.EAST);
        v64 = 100000000;
        d64 = null;

        l53 = l54.add(Direction.SOUTH);
        v53 = 100000000;
        d53 = null;

        l55 = l54.add(Direction.NORTH);
        v55 = 100000000;
        d55 = null;

        l43 = l44.add(Direction.SOUTH);
        v43 = 100000000;
        d43 = null;

        l45 = l55.add(Direction.WEST);
        v45 = 100000000;
        d45 = null;

        l63 = l53.add(Direction.EAST);
        v63 = 100000000;
        d63 = null;

        l65 = l55.add(Direction.EAST);
        v65 = 100000000;
        d65 = null;

        l34 = l44.add(Direction.WEST);
        v34 = 100000000;
        d34 = null;

        l74 = l64.add(Direction.EAST);
        v74 = 100000000;
        d74 = null;

        l52 = l53.add(Direction.SOUTH);
        v52 = 100000000;
        d52 = null;

        l56 = l55.add(Direction.NORTH);
        v56 = 100000000;
        d56 = null;

        l33 = l34.add(Direction.SOUTH);
        v33 = 100000000;
        d33 = null;

        l35 = l45.add(Direction.WEST);
        v35 = 100000000;
        d35 = null;

        l73 = l63.add(Direction.EAST);
        v73 = 100000000;
        d73 = null;

        l75 = l65.add(Direction.EAST);
        v75 = 100000000;
        d75 = null;

        l42 = l43.add(Direction.SOUTH);
        v42 = 100000000;
        d42 = null;

        l46 = l56.add(Direction.WEST);
        v46 = 100000000;
        d46 = null;

        l62 = l52.add(Direction.EAST);
        v62 = 100000000;
        d62 = null;

        l66 = l56.add(Direction.EAST);
        v66 = 100000000;
        d66 = null;

        l32 = l33.add(Direction.SOUTH);
        v32 = 100000000;
        d32 = null;

        l36 = l46.add(Direction.WEST);
        v36 = 100000000;
        d36 = null;

        l72 = l62.add(Direction.EAST);
        v72 = 100000000;
        d72 = null;

        l76 = l66.add(Direction.EAST);
        v76 = 100000000;
        d76 = null;

        l24 = l34.add(Direction.WEST);
        v24 = 100000000;
        d24 = null;

        l84 = l74.add(Direction.EAST);
        v84 = 100000000;
        d84 = null;

        l51 = l52.add(Direction.SOUTH);
        v51 = 100000000;
        d51 = null;

        l57 = l56.add(Direction.NORTH);
        v57 = 100000000;
        d57 = null;

        l23 = l24.add(Direction.SOUTH);
        v23 = 100000000;
        d23 = null;

        l25 = l35.add(Direction.WEST);
        v25 = 100000000;
        d25 = null;

        l83 = l73.add(Direction.EAST);
        v83 = 100000000;
        d83 = null;

        l85 = l75.add(Direction.EAST);
        v85 = 100000000;
        d85 = null;

        l41 = l42.add(Direction.SOUTH);
        v41 = 100000000;
        d41 = null;

        l47 = l57.add(Direction.WEST);
        v47 = 100000000;
        d47 = null;

        l61 = l51.add(Direction.EAST);
        v61 = 100000000;
        d61 = null;

        l67 = l57.add(Direction.EAST);
        v67 = 100000000;
        d67 = null;

        l22 = l23.add(Direction.SOUTH);
        v22 = 100000000;
        d22 = null;

        l26 = l36.add(Direction.WEST);
        v26 = 100000000;
        d26 = null;

        l82 = l72.add(Direction.EAST);
        v82 = 100000000;
        d82 = null;

        l86 = l76.add(Direction.EAST);
        v86 = 100000000;
        d86 = null;

        l31 = l32.add(Direction.SOUTH);
        v31 = 100000000;
        d31 = null;

        l37 = l47.add(Direction.WEST);
        v37 = 100000000;
        d37 = null;

        l71 = l61.add(Direction.EAST);
        v71 = 100000000;
        d71 = null;

        l77 = l67.add(Direction.EAST);
        v77 = 100000000;
        d77 = null;

        l21 = l22.add(Direction.SOUTH);
        v21 = 100000000;
        d21 = null;

        l27 = l37.add(Direction.WEST);
        v27 = 100000000;
        d27 = null;

        l81 = l71.add(Direction.EAST);
        v81 = 100000000;
        d81 = null;

        l87 = l77.add(Direction.EAST);
        v87 = 100000000;
        d87 = null;

        l14 = l24.add(Direction.WEST);
        v14 = 100000000;
        d14 = null;

        l94 = l84.add(Direction.EAST);
        v94 = 100000000;
        d94 = null;

        l50 = l51.add(Direction.SOUTH);
        v50 = 100000000;
        d50 = null;

        l58 = l57.add(Direction.NORTH);
        v58 = 100000000;
        d58 = null;

        l13 = l14.add(Direction.SOUTH);
        v13 = 100000000;
        d13 = null;

        l15 = l25.add(Direction.WEST);
        v15 = 100000000;
        d15 = null;

        l93 = l83.add(Direction.EAST);
        v93 = 100000000;
        d93 = null;

        l95 = l85.add(Direction.EAST);
        v95 = 100000000;
        d95 = null;

        l40 = l41.add(Direction.SOUTH);
        v40 = 100000000;
        d40 = null;

        l48 = l58.add(Direction.WEST);
        v48 = 100000000;
        d48 = null;

        l60 = l50.add(Direction.EAST);
        v60 = 100000000;
        d60 = null;

        l68 = l58.add(Direction.EAST);
        v68 = 100000000;
        d68 = null;

        l12 = l13.add(Direction.SOUTH);
        v12 = 100000000;
        d12 = null;

        l16 = l26.add(Direction.WEST);
        v16 = 100000000;
        d16 = null;

        l92 = l82.add(Direction.EAST);
        v92 = 100000000;
        d92 = null;

        l96 = l86.add(Direction.EAST);
        v96 = 100000000;
        d96 = null;

        l30 = l31.add(Direction.SOUTH);
        v30 = 100000000;
        d30 = null;

        l38 = l48.add(Direction.WEST);
        v38 = 100000000;
        d38 = null;

        l70 = l60.add(Direction.EAST);
        v70 = 100000000;
        d70 = null;

        l78 = l68.add(Direction.EAST);
        v78 = 100000000;
        d78 = null;

        System.out.println("Initialized vars: " + Clock.getBytecodesLeft());
        try{ double sum;if(rc.onTheMap(l44)){
            if(!rc.isLocationOccupied(l44)){
                p44 = 20*(rc.senseRubble(l44)/10 + 1);
                sum = v54 + p44;
                if(v44 > sum){
                    v44 = sum;
                    d44 = Direction.WEST;
                }
            }
        }
            if(rc.onTheMap(l64)){
                if(!rc.isLocationOccupied(l64)){
                    p64 = 20*(rc.senseRubble(l64)/10 + 1);
                    sum = v54 + p64;
                    if(v64 > sum){
                        v64 = sum;
                        d64 = Direction.EAST;
                    }
                }
            }
            if(rc.onTheMap(l53)){
                if(!rc.isLocationOccupied(l53)){
                    p53 = 20*(rc.senseRubble(l53)/10 + 1);
                    sum = v54 + p53;
                    if(v53 > sum){
                        v53 = sum;
                        d53 = Direction.SOUTH;
                    }
                    sum = v44 + p53;
                    if(v53 > sum){
                        v53 = sum;
                        d53 = d44;
                    }
                    sum = v64 + p53;
                    if(v53 > sum){
                        v53 = sum;
                        d53 = d64;
                    }
                }
            }
            if(rc.onTheMap(l55)){
                if(!rc.isLocationOccupied(l55)){
                    p55 = 20*(rc.senseRubble(l55)/10 + 1);
                    sum = v54 + p55;
                    if(v55 > sum){
                        v55 = sum;
                        d55 = Direction.NORTH;
                    }
                    sum = v44 + p55;
                    if(v55 > sum){
                        v55 = sum;
                        d55 = d44;
                    }
                    sum = v64 + p55;
                    if(v55 > sum){
                        v55 = sum;
                        d55 = d64;
                    }
                }
            }
            if(rc.onTheMap(l43)){
                if(!rc.isLocationOccupied(l43)){
                    p43 = 20*(rc.senseRubble(l43)/10 + 1);
                    sum = v54 + p43;
                    if(v43 > sum){
                        v43 = sum;
                        d43 = Direction.SOUTHWEST;
                    }
                    sum = v44 + p43;
                    if(v43 > sum){
                        v43 = sum;
                        d43 = d44;
                    }
                    sum = v53 + p43;
                    if(v43 > sum){
                        v43 = sum;
                        d43 = d53;
                    }
                }
            }
            if(rc.onTheMap(l45)){
                if(!rc.isLocationOccupied(l45)){
                    p45 = 20*(rc.senseRubble(l45)/10 + 1);
                    sum = v54 + p45;
                    if(v45 > sum){
                        v45 = sum;
                        d45 = Direction.NORTHWEST;
                    }
                    sum = v55 + p45;
                    if(v45 > sum){
                        v45 = sum;
                        d45 = d55;
                    }
                    sum = v44 + p45;
                    if(v45 > sum){
                        v45 = sum;
                        d45 = d44;
                    }
                }
            }
            if(rc.onTheMap(l63)){
                if(!rc.isLocationOccupied(l63)){
                    p63 = 20*(rc.senseRubble(l63)/10 + 1);
                    sum = v54 + p63;
                    if(v63 > sum){
                        v63 = sum;
                        d63 = Direction.SOUTHEAST;
                    }
                    sum = v53 + p63;
                    if(v63 > sum){
                        v63 = sum;
                        d63 = d53;
                    }
                    sum = v64 + p63;
                    if(v63 > sum){
                        v63 = sum;
                        d63 = d64;
                    }
                }
            }
            if(rc.onTheMap(l65)){
                if(!rc.isLocationOccupied(l65)){
                    p65 = 20*(rc.senseRubble(l65)/10 + 1);
                    sum = v54 + p65;
                    if(v65 > sum){
                        v65 = sum;
                        d65 = Direction.NORTHEAST;
                    }
                    sum = v55 + p65;
                    if(v65 > sum){
                        v65 = sum;
                        d65 = d55;
                    }
                    sum = v64 + p65;
                    if(v65 > sum){
                        v65 = sum;
                        d65 = d64;
                    }
                }
            }
            if(rc.onTheMap(l34)){
                p34 = 20*(rc.senseRubble(l34)/10 + 1);
                sum = v44 + p34;
                if(v34 > sum){
                    v34 = sum;
                    d34 = d44;
                }
                sum = v45 + p34;
                if(v34 > sum){
                    v34 = sum;
                    d34 = d45;
                }
                sum = v43 + p34;
                if(v34 > sum){
                    v34 = sum;
                    d34 = d43;
                }
            }
            if(rc.onTheMap(l74)){
                p74 = 20*(rc.senseRubble(l74)/10 + 1);
                sum = v64 + p74;
                if(v74 > sum){
                    v74 = sum;
                    d74 = d64;
                }
                sum = v63 + p74;
                if(v74 > sum){
                    v74 = sum;
                    d74 = d63;
                }
                sum = v65 + p74;
                if(v74 > sum){
                    v74 = sum;
                    d74 = d65;
                }
            }
            if(rc.onTheMap(l52)){
                p52 = 20*(rc.senseRubble(l52)/10 + 1);
                sum = v53 + p52;
                if(v52 > sum){
                    v52 = sum;
                    d52 = d53;
                }
                sum = v43 + p52;
                if(v52 > sum){
                    v52 = sum;
                    d52 = d43;
                }
                sum = v63 + p52;
                if(v52 > sum){
                    v52 = sum;
                    d52 = d63;
                }
            }
            if(rc.onTheMap(l56)){
                p56 = 20*(rc.senseRubble(l56)/10 + 1);
                sum = v55 + p56;
                if(v56 > sum){
                    v56 = sum;
                    d56 = d55;
                }
                sum = v45 + p56;
                if(v56 > sum){
                    v56 = sum;
                    d56 = d45;
                }
                sum = v65 + p56;
                if(v56 > sum){
                    v56 = sum;
                    d56 = d65;
                }
            }
            if(rc.onTheMap(l33)){
                p33 = 20*(rc.senseRubble(l33)/10 + 1);
                sum = v44 + p33;
                if(v33 > sum){
                    v33 = sum;
                    d33 = d44;
                }
                sum = v43 + p33;
                if(v33 > sum){
                    v33 = sum;
                    d33 = d43;
                }
                sum = v34 + p33;
                if(v33 > sum){
                    v33 = sum;
                    d33 = d34;
                }
            }
            if(rc.onTheMap(l35)){
                p35 = 20*(rc.senseRubble(l35)/10 + 1);
                sum = v44 + p35;
                if(v35 > sum){
                    v35 = sum;
                    d35 = d44;
                }
                sum = v45 + p35;
                if(v35 > sum){
                    v35 = sum;
                    d35 = d45;
                }
                sum = v34 + p35;
                if(v35 > sum){
                    v35 = sum;
                    d35 = d34;
                }
            }
            if(rc.onTheMap(l73)){
                p73 = 20*(rc.senseRubble(l73)/10 + 1);
                sum = v64 + p73;
                if(v73 > sum){
                    v73 = sum;
                    d73 = d64;
                }
                sum = v63 + p73;
                if(v73 > sum){
                    v73 = sum;
                    d73 = d63;
                }
                sum = v74 + p73;
                if(v73 > sum){
                    v73 = sum;
                    d73 = d74;
                }
            }
            if(rc.onTheMap(l75)){
                p75 = 20*(rc.senseRubble(l75)/10 + 1);
                sum = v64 + p75;
                if(v75 > sum){
                    v75 = sum;
                    d75 = d64;
                }
                sum = v65 + p75;
                if(v75 > sum){
                    v75 = sum;
                    d75 = d65;
                }
                sum = v74 + p75;
                if(v75 > sum){
                    v75 = sum;
                    d75 = d74;
                }
            }
            if(rc.onTheMap(l42)){
                p42 = 20*(rc.senseRubble(l42)/10 + 1);
                sum = v53 + p42;
                if(v42 > sum){
                    v42 = sum;
                    d42 = d53;
                }
                sum = v43 + p42;
                if(v42 > sum){
                    v42 = sum;
                    d42 = d43;
                }
                sum = v52 + p42;
                if(v42 > sum){
                    v42 = sum;
                    d42 = d52;
                }
                sum = v33 + p42;
                if(v42 > sum){
                    v42 = sum;
                    d42 = d33;
                }
            }
            if(rc.onTheMap(l46)){
                p46 = 20*(rc.senseRubble(l46)/10 + 1);
                sum = v55 + p46;
                if(v46 > sum){
                    v46 = sum;
                    d46 = d55;
                }
                sum = v45 + p46;
                if(v46 > sum){
                    v46 = sum;
                    d46 = d45;
                }
                sum = v56 + p46;
                if(v46 > sum){
                    v46 = sum;
                    d46 = d56;
                }
                sum = v35 + p46;
                if(v46 > sum){
                    v46 = sum;
                    d46 = d35;
                }
            }
            if(rc.onTheMap(l62)){
                p62 = 20*(rc.senseRubble(l62)/10 + 1);
                sum = v53 + p62;
                if(v62 > sum){
                    v62 = sum;
                    d62 = d53;
                }
                sum = v63 + p62;
                if(v62 > sum){
                    v62 = sum;
                    d62 = d63;
                }
                sum = v52 + p62;
                if(v62 > sum){
                    v62 = sum;
                    d62 = d52;
                }
                sum = v73 + p62;
                if(v62 > sum){
                    v62 = sum;
                    d62 = d73;
                }
            }
            if(rc.onTheMap(l66)){
                p66 = 20*(rc.senseRubble(l66)/10 + 1);
                sum = v55 + p66;
                if(v66 > sum){
                    v66 = sum;
                    d66 = d55;
                }
                sum = v65 + p66;
                if(v66 > sum){
                    v66 = sum;
                    d66 = d65;
                }
                sum = v56 + p66;
                if(v66 > sum){
                    v66 = sum;
                    d66 = d56;
                }
                sum = v75 + p66;
                if(v66 > sum){
                    v66 = sum;
                    d66 = d75;
                }
            }
            if(rc.onTheMap(l32)){
                p32 = 20*(rc.senseRubble(l32)/10 + 1);
                sum = v43 + p32;
                if(v32 > sum){
                    v32 = sum;
                    d32 = d43;
                }
                sum = v33 + p32;
                if(v32 > sum){
                    v32 = sum;
                    d32 = d33;
                }
                sum = v42 + p32;
                if(v32 > sum){
                    v32 = sum;
                    d32 = d42;
                }
            }
            if(rc.onTheMap(l36)){
                p36 = 20*(rc.senseRubble(l36)/10 + 1);
                sum = v45 + p36;
                if(v36 > sum){
                    v36 = sum;
                    d36 = d45;
                }
                sum = v46 + p36;
                if(v36 > sum){
                    v36 = sum;
                    d36 = d46;
                }
                sum = v35 + p36;
                if(v36 > sum){
                    v36 = sum;
                    d36 = d35;
                }
            }
            if(rc.onTheMap(l72)){
                p72 = 20*(rc.senseRubble(l72)/10 + 1);
                sum = v63 + p72;
                if(v72 > sum){
                    v72 = sum;
                    d72 = d63;
                }
                sum = v62 + p72;
                if(v72 > sum){
                    v72 = sum;
                    d72 = d62;
                }
                sum = v73 + p72;
                if(v72 > sum){
                    v72 = sum;
                    d72 = d73;
                }
            }
            if(rc.onTheMap(l76)){
                p76 = 20*(rc.senseRubble(l76)/10 + 1);
                sum = v65 + p76;
                if(v76 > sum){
                    v76 = sum;
                    d76 = d65;
                }
                sum = v66 + p76;
                if(v76 > sum){
                    v76 = sum;
                    d76 = d66;
                }
                sum = v75 + p76;
                if(v76 > sum){
                    v76 = sum;
                    d76 = d75;
                }
            }
            if(rc.onTheMap(l24)){
                p24 = 20*(rc.senseRubble(l24)/10 + 1);
                sum = v34 + p24;
                if(v24 > sum){
                    v24 = sum;
                    d24 = d34;
                }
                sum = v35 + p24;
                if(v24 > sum){
                    v24 = sum;
                    d24 = d35;
                }
                sum = v33 + p24;
                if(v24 > sum){
                    v24 = sum;
                    d24 = d33;
                }
            }
            if(rc.onTheMap(l84)){
                p84 = 20*(rc.senseRubble(l84)/10 + 1);
                sum = v74 + p84;
                if(v84 > sum){
                    v84 = sum;
                    d84 = d74;
                }
                sum = v73 + p84;
                if(v84 > sum){
                    v84 = sum;
                    d84 = d73;
                }
                sum = v75 + p84;
                if(v84 > sum){
                    v84 = sum;
                    d84 = d75;
                }
            }
            if(rc.onTheMap(l51)){
                p51 = 20*(rc.senseRubble(l51)/10 + 1);
                sum = v52 + p51;
                if(v51 > sum){
                    v51 = sum;
                    d51 = d52;
                }
                sum = v42 + p51;
                if(v51 > sum){
                    v51 = sum;
                    d51 = d42;
                }
                sum = v62 + p51;
                if(v51 > sum){
                    v51 = sum;
                    d51 = d62;
                }
            }
            if(rc.onTheMap(l57)){
                p57 = 20*(rc.senseRubble(l57)/10 + 1);
                sum = v56 + p57;
                if(v57 > sum){
                    v57 = sum;
                    d57 = d56;
                }
                sum = v46 + p57;
                if(v57 > sum){
                    v57 = sum;
                    d57 = d46;
                }
                sum = v66 + p57;
                if(v57 > sum){
                    v57 = sum;
                    d57 = d66;
                }
            }
            if(rc.onTheMap(l23)){
                p23 = 20*(rc.senseRubble(l23)/10 + 1);
                sum = v34 + p23;
                if(v23 > sum){
                    v23 = sum;
                    d23 = d34;
                }
                sum = v33 + p23;
                if(v23 > sum){
                    v23 = sum;
                    d23 = d33;
                }
                sum = v32 + p23;
                if(v23 > sum){
                    v23 = sum;
                    d23 = d32;
                }
                sum = v24 + p23;
                if(v23 > sum){
                    v23 = sum;
                    d23 = d24;
                }
            }
            if(rc.onTheMap(l25)){
                p25 = 20*(rc.senseRubble(l25)/10 + 1);
                sum = v34 + p25;
                if(v25 > sum){
                    v25 = sum;
                    d25 = d34;
                }
                sum = v35 + p25;
                if(v25 > sum){
                    v25 = sum;
                    d25 = d35;
                }
                sum = v36 + p25;
                if(v25 > sum){
                    v25 = sum;
                    d25 = d36;
                }
                sum = v24 + p25;
                if(v25 > sum){
                    v25 = sum;
                    d25 = d24;
                }
            }
            if(rc.onTheMap(l83)){
                p83 = 20*(rc.senseRubble(l83)/10 + 1);
                sum = v74 + p83;
                if(v83 > sum){
                    v83 = sum;
                    d83 = d74;
                }
                sum = v73 + p83;
                if(v83 > sum){
                    v83 = sum;
                    d83 = d73;
                }
                sum = v72 + p83;
                if(v83 > sum){
                    v83 = sum;
                    d83 = d72;
                }
                sum = v84 + p83;
                if(v83 > sum){
                    v83 = sum;
                    d83 = d84;
                }
            }
            if(rc.onTheMap(l85)){
                p85 = 20*(rc.senseRubble(l85)/10 + 1);
                sum = v74 + p85;
                if(v85 > sum){
                    v85 = sum;
                    d85 = d74;
                }
                sum = v75 + p85;
                if(v85 > sum){
                    v85 = sum;
                    d85 = d75;
                }
                sum = v76 + p85;
                if(v85 > sum){
                    v85 = sum;
                    d85 = d76;
                }
                sum = v84 + p85;
                if(v85 > sum){
                    v85 = sum;
                    d85 = d84;
                }
            }
            if(rc.onTheMap(l41)){
                p41 = 20*(rc.senseRubble(l41)/10 + 1);
                sum = v52 + p41;
                if(v41 > sum){
                    v41 = sum;
                    d41 = d52;
                }
                sum = v42 + p41;
                if(v41 > sum){
                    v41 = sum;
                    d41 = d42;
                }
                sum = v32 + p41;
                if(v41 > sum){
                    v41 = sum;
                    d41 = d32;
                }
                sum = v51 + p41;
                if(v41 > sum){
                    v41 = sum;
                    d41 = d51;
                }
            }
            if(rc.onTheMap(l47)){
                p47 = 20*(rc.senseRubble(l47)/10 + 1);
                sum = v56 + p47;
                if(v47 > sum){
                    v47 = sum;
                    d47 = d56;
                }
                sum = v46 + p47;
                if(v47 > sum){
                    v47 = sum;
                    d47 = d46;
                }
                sum = v36 + p47;
                if(v47 > sum){
                    v47 = sum;
                    d47 = d36;
                }
                sum = v57 + p47;
                if(v47 > sum){
                    v47 = sum;
                    d47 = d57;
                }
            }
            if(rc.onTheMap(l61)){
                p61 = 20*(rc.senseRubble(l61)/10 + 1);
                sum = v52 + p61;
                if(v61 > sum){
                    v61 = sum;
                    d61 = d52;
                }
                sum = v62 + p61;
                if(v61 > sum){
                    v61 = sum;
                    d61 = d62;
                }
                sum = v72 + p61;
                if(v61 > sum){
                    v61 = sum;
                    d61 = d72;
                }
                sum = v51 + p61;
                if(v61 > sum){
                    v61 = sum;
                    d61 = d51;
                }
            }
            if(rc.onTheMap(l67)){
                p67 = 20*(rc.senseRubble(l67)/10 + 1);
                sum = v56 + p67;
                if(v67 > sum){
                    v67 = sum;
                    d67 = d56;
                }
                sum = v66 + p67;
                if(v67 > sum){
                    v67 = sum;
                    d67 = d66;
                }
                sum = v76 + p67;
                if(v67 > sum){
                    v67 = sum;
                    d67 = d76;
                }
                sum = v57 + p67;
                if(v67 > sum){
                    v67 = sum;
                    d67 = d57;
                }
            }
            if(rc.onTheMap(l22)){
                p22 = 20*(rc.senseRubble(l22)/10 + 1);
                sum = v33 + p22;
                if(v22 > sum){
                    v22 = sum;
                    d22 = d33;
                }
                sum = v32 + p22;
                if(v22 > sum){
                    v22 = sum;
                    d22 = d32;
                }
                sum = v23 + p22;
                if(v22 > sum){
                    v22 = sum;
                    d22 = d23;
                }
            }
            if(rc.onTheMap(l26)){
                p26 = 20*(rc.senseRubble(l26)/10 + 1);
                sum = v35 + p26;
                if(v26 > sum){
                    v26 = sum;
                    d26 = d35;
                }
                sum = v36 + p26;
                if(v26 > sum){
                    v26 = sum;
                    d26 = d36;
                }
                sum = v25 + p26;
                if(v26 > sum){
                    v26 = sum;
                    d26 = d25;
                }
            }
            if(rc.onTheMap(l82)){
                p82 = 20*(rc.senseRubble(l82)/10 + 1);
                sum = v73 + p82;
                if(v82 > sum){
                    v82 = sum;
                    d82 = d73;
                }
                sum = v72 + p82;
                if(v82 > sum){
                    v82 = sum;
                    d82 = d72;
                }
                sum = v83 + p82;
                if(v82 > sum){
                    v82 = sum;
                    d82 = d83;
                }
            }
            if(rc.onTheMap(l86)){
                p86 = 20*(rc.senseRubble(l86)/10 + 1);
                sum = v75 + p86;
                if(v86 > sum){
                    v86 = sum;
                    d86 = d75;
                }
                sum = v76 + p86;
                if(v86 > sum){
                    v86 = sum;
                    d86 = d76;
                }
                sum = v85 + p86;
                if(v86 > sum){
                    v86 = sum;
                    d86 = d85;
                }
            }
            if(rc.onTheMap(l31)){
                p31 = 20*(rc.senseRubble(l31)/10 + 1);
                sum = v42 + p31;
                if(v31 > sum){
                    v31 = sum;
                    d31 = d42;
                }
                sum = v32 + p31;
                if(v31 > sum){
                    v31 = sum;
                    d31 = d32;
                }
                sum = v41 + p31;
                if(v31 > sum){
                    v31 = sum;
                    d31 = d41;
                }
                sum = v22 + p31;
                if(v31 > sum){
                    v31 = sum;
                    d31 = d22;
                }
            }
            if(rc.onTheMap(l37)){
                p37 = 20*(rc.senseRubble(l37)/10 + 1);
                sum = v46 + p37;
                if(v37 > sum){
                    v37 = sum;
                    d37 = d46;
                }
                sum = v36 + p37;
                if(v37 > sum){
                    v37 = sum;
                    d37 = d36;
                }
                sum = v47 + p37;
                if(v37 > sum){
                    v37 = sum;
                    d37 = d47;
                }
                sum = v26 + p37;
                if(v37 > sum){
                    v37 = sum;
                    d37 = d26;
                }
            }
            if(rc.onTheMap(l71)){
                p71 = 20*(rc.senseRubble(l71)/10 + 1);
                sum = v62 + p71;
                if(v71 > sum){
                    v71 = sum;
                    d71 = d62;
                }
                sum = v72 + p71;
                if(v71 > sum){
                    v71 = sum;
                    d71 = d72;
                }
                sum = v61 + p71;
                if(v71 > sum){
                    v71 = sum;
                    d71 = d61;
                }
                sum = v82 + p71;
                if(v71 > sum){
                    v71 = sum;
                    d71 = d82;
                }
            }
            if(rc.onTheMap(l77)){
                p77 = 20*(rc.senseRubble(l77)/10 + 1);
                sum = v66 + p77;
                if(v77 > sum){
                    v77 = sum;
                    d77 = d66;
                }
                sum = v76 + p77;
                if(v77 > sum){
                    v77 = sum;
                    d77 = d76;
                }
                sum = v67 + p77;
                if(v77 > sum){
                    v77 = sum;
                    d77 = d67;
                }
                sum = v86 + p77;
                if(v77 > sum){
                    v77 = sum;
                    d77 = d86;
                }
            }
            if(rc.onTheMap(l21)){
                p21 = 20*(rc.senseRubble(l21)/10 + 1);
                sum = v32 + p21;
                if(v21 > sum){
                    v21 = sum;
                    d21 = d32;
                }
                sum = v22 + p21;
                if(v21 > sum){
                    v21 = sum;
                    d21 = d22;
                }
                sum = v31 + p21;
                if(v21 > sum){
                    v21 = sum;
                    d21 = d31;
                }
            }
            if(rc.onTheMap(l27)){
                p27 = 20*(rc.senseRubble(l27)/10 + 1);
                sum = v36 + p27;
                if(v27 > sum){
                    v27 = sum;
                    d27 = d36;
                }
                sum = v37 + p27;
                if(v27 > sum){
                    v27 = sum;
                    d27 = d37;
                }
                sum = v26 + p27;
                if(v27 > sum){
                    v27 = sum;
                    d27 = d26;
                }
            }
            if(rc.onTheMap(l81)){
                p81 = 20*(rc.senseRubble(l81)/10 + 1);
                sum = v72 + p81;
                if(v81 > sum){
                    v81 = sum;
                    d81 = d72;
                }
                sum = v71 + p81;
                if(v81 > sum){
                    v81 = sum;
                    d81 = d71;
                }
                sum = v82 + p81;
                if(v81 > sum){
                    v81 = sum;
                    d81 = d82;
                }
            }
            if(rc.onTheMap(l87)){
                p87 = 20*(rc.senseRubble(l87)/10 + 1);
                sum = v76 + p87;
                if(v87 > sum){
                    v87 = sum;
                    d87 = d76;
                }
                sum = v77 + p87;
                if(v87 > sum){
                    v87 = sum;
                    d87 = d77;
                }
                sum = v86 + p87;
                if(v87 > sum){
                    v87 = sum;
                    d87 = d86;
                }
            }
            if(rc.onTheMap(l14)){
                p14 = 20*(rc.senseRubble(l14)/10 + 1);
                sum = v24 + p14;
                if(v14 > sum){
                    v14 = sum;
                    d14 = d24;
                }
                sum = v25 + p14;
                if(v14 > sum){
                    v14 = sum;
                    d14 = d25;
                }
                sum = v23 + p14;
                if(v14 > sum){
                    v14 = sum;
                    d14 = d23;
                }
            }
            if(rc.onTheMap(l94)){
                p94 = 20*(rc.senseRubble(l94)/10 + 1);
                sum = v84 + p94;
                if(v94 > sum){
                    v94 = sum;
                    d94 = d84;
                }
                sum = v83 + p94;
                if(v94 > sum){
                    v94 = sum;
                    d94 = d83;
                }
                sum = v85 + p94;
                if(v94 > sum){
                    v94 = sum;
                    d94 = d85;
                }
            }
            if(rc.onTheMap(l50)){
                p50 = 20*(rc.senseRubble(l50)/10 + 1);
                sum = v51 + p50;
                if(v50 > sum){
                    v50 = sum;
                    d50 = d51;
                }
                sum = v41 + p50;
                if(v50 > sum){
                    v50 = sum;
                    d50 = d41;
                }
                sum = v61 + p50;
                if(v50 > sum){
                    v50 = sum;
                    d50 = d61;
                }
            }
            if(rc.onTheMap(l58)){
                p58 = 20*(rc.senseRubble(l58)/10 + 1);
                sum = v57 + p58;
                if(v58 > sum){
                    v58 = sum;
                    d58 = d57;
                }
                sum = v47 + p58;
                if(v58 > sum){
                    v58 = sum;
                    d58 = d47;
                }
                sum = v67 + p58;
                if(v58 > sum){
                    v58 = sum;
                    d58 = d67;
                }
            }
            if(rc.onTheMap(l13)){
                p13 = 20*(rc.senseRubble(l13)/10 + 1);
                sum = v24 + p13;
                if(v13 > sum){
                    v13 = sum;
                    d13 = d24;
                }
                sum = v23 + p13;
                if(v13 > sum){
                    v13 = sum;
                    d13 = d23;
                }
                sum = v22 + p13;
                if(v13 > sum){
                    v13 = sum;
                    d13 = d22;
                }
                sum = v14 + p13;
                if(v13 > sum){
                    v13 = sum;
                    d13 = d14;
                }
            }
            if(rc.onTheMap(l15)){
                p15 = 20*(rc.senseRubble(l15)/10 + 1);
                sum = v24 + p15;
                if(v15 > sum){
                    v15 = sum;
                    d15 = d24;
                }
                sum = v25 + p15;
                if(v15 > sum){
                    v15 = sum;
                    d15 = d25;
                }
                sum = v26 + p15;
                if(v15 > sum){
                    v15 = sum;
                    d15 = d26;
                }
                sum = v14 + p15;
                if(v15 > sum){
                    v15 = sum;
                    d15 = d14;
                }
            }
            if(rc.onTheMap(l93)){
                p93 = 20*(rc.senseRubble(l93)/10 + 1);
                sum = v84 + p93;
                if(v93 > sum){
                    v93 = sum;
                    d93 = d84;
                }
                sum = v83 + p93;
                if(v93 > sum){
                    v93 = sum;
                    d93 = d83;
                }
                sum = v82 + p93;
                if(v93 > sum){
                    v93 = sum;
                    d93 = d82;
                }
                sum = v94 + p93;
                if(v93 > sum){
                    v93 = sum;
                    d93 = d94;
                }
            }
            if(rc.onTheMap(l95)){
                p95 = 20*(rc.senseRubble(l95)/10 + 1);
                sum = v84 + p95;
                if(v95 > sum){
                    v95 = sum;
                    d95 = d84;
                }
                sum = v85 + p95;
                if(v95 > sum){
                    v95 = sum;
                    d95 = d85;
                }
                sum = v86 + p95;
                if(v95 > sum){
                    v95 = sum;
                    d95 = d86;
                }
                sum = v94 + p95;
                if(v95 > sum){
                    v95 = sum;
                    d95 = d94;
                }
            }
            if(rc.onTheMap(l40)){
                p40 = 20*(rc.senseRubble(l40)/10 + 1);
                sum = v51 + p40;
                if(v40 > sum){
                    v40 = sum;
                    d40 = d51;
                }
                sum = v41 + p40;
                if(v40 > sum){
                    v40 = sum;
                    d40 = d41;
                }
                sum = v31 + p40;
                if(v40 > sum){
                    v40 = sum;
                    d40 = d31;
                }
                sum = v50 + p40;
                if(v40 > sum){
                    v40 = sum;
                    d40 = d50;
                }
            }
            if(rc.onTheMap(l48)){
                p48 = 20*(rc.senseRubble(l48)/10 + 1);
                sum = v57 + p48;
                if(v48 > sum){
                    v48 = sum;
                    d48 = d57;
                }
                sum = v47 + p48;
                if(v48 > sum){
                    v48 = sum;
                    d48 = d47;
                }
                sum = v37 + p48;
                if(v48 > sum){
                    v48 = sum;
                    d48 = d37;
                }
                sum = v58 + p48;
                if(v48 > sum){
                    v48 = sum;
                    d48 = d58;
                }
            }
            if(rc.onTheMap(l60)){
                p60 = 20*(rc.senseRubble(l60)/10 + 1);
                sum = v51 + p60;
                if(v60 > sum){
                    v60 = sum;
                    d60 = d51;
                }
                sum = v61 + p60;
                if(v60 > sum){
                    v60 = sum;
                    d60 = d61;
                }
                sum = v71 + p60;
                if(v60 > sum){
                    v60 = sum;
                    d60 = d71;
                }
                sum = v50 + p60;
                if(v60 > sum){
                    v60 = sum;
                    d60 = d50;
                }
            }
            if(rc.onTheMap(l68)){
                p68 = 20*(rc.senseRubble(l68)/10 + 1);
                sum = v57 + p68;
                if(v68 > sum){
                    v68 = sum;
                    d68 = d57;
                }
                sum = v67 + p68;
                if(v68 > sum){
                    v68 = sum;
                    d68 = d67;
                }
                sum = v77 + p68;
                if(v68 > sum){
                    v68 = sum;
                    d68 = d77;
                }
                sum = v58 + p68;
                if(v68 > sum){
                    v68 = sum;
                    d68 = d58;
                }
            }
            if(rc.onTheMap(l12)){
                p12 = 20*(rc.senseRubble(l12)/10 + 1);
                sum = v23 + p12;
                if(v12 > sum){
                    v12 = sum;
                    d12 = d23;
                }
                sum = v22 + p12;
                if(v12 > sum){
                    v12 = sum;
                    d12 = d22;
                }
                sum = v13 + p12;
                if(v12 > sum){
                    v12 = sum;
                    d12 = d13;
                }
                sum = v21 + p12;
                if(v12 > sum){
                    v12 = sum;
                    d12 = d21;
                }
            }
            if(rc.onTheMap(l16)){
                p16 = 20*(rc.senseRubble(l16)/10 + 1);
                sum = v25 + p16;
                if(v16 > sum){
                    v16 = sum;
                    d16 = d25;
                }
                sum = v26 + p16;
                if(v16 > sum){
                    v16 = sum;
                    d16 = d26;
                }
                sum = v15 + p16;
                if(v16 > sum){
                    v16 = sum;
                    d16 = d15;
                }
                sum = v27 + p16;
                if(v16 > sum){
                    v16 = sum;
                    d16 = d27;
                }
            }
            if(rc.onTheMap(l92)){
                p92 = 20*(rc.senseRubble(l92)/10 + 1);
                sum = v83 + p92;
                if(v92 > sum){
                    v92 = sum;
                    d92 = d83;
                }
                sum = v82 + p92;
                if(v92 > sum){
                    v92 = sum;
                    d92 = d82;
                }
                sum = v93 + p92;
                if(v92 > sum){
                    v92 = sum;
                    d92 = d93;
                }
                sum = v81 + p92;
                if(v92 > sum){
                    v92 = sum;
                    d92 = d81;
                }
            }
            if(rc.onTheMap(l96)){
                p96 = 20*(rc.senseRubble(l96)/10 + 1);
                sum = v85 + p96;
                if(v96 > sum){
                    v96 = sum;
                    d96 = d85;
                }
                sum = v86 + p96;
                if(v96 > sum){
                    v96 = sum;
                    d96 = d86;
                }
                sum = v95 + p96;
                if(v96 > sum){
                    v96 = sum;
                    d96 = d95;
                }
                sum = v87 + p96;
                if(v96 > sum){
                    v96 = sum;
                    d96 = d87;
                }
            }
            if(rc.onTheMap(l30)){
                p30 = 20*(rc.senseRubble(l30)/10 + 1);
                sum = v41 + p30;
                if(v30 > sum){
                    v30 = sum;
                    d30 = d41;
                }
                sum = v31 + p30;
                if(v30 > sum){
                    v30 = sum;
                    d30 = d31;
                }
                sum = v40 + p30;
                if(v30 > sum){
                    v30 = sum;
                    d30 = d40;
                }
                sum = v21 + p30;
                if(v30 > sum){
                    v30 = sum;
                    d30 = d21;
                }
            }
            if(rc.onTheMap(l38)){
                p38 = 20*(rc.senseRubble(l38)/10 + 1);
                sum = v47 + p38;
                if(v38 > sum){
                    v38 = sum;
                    d38 = d47;
                }
                sum = v37 + p38;
                if(v38 > sum){
                    v38 = sum;
                    d38 = d37;
                }
                sum = v48 + p38;
                if(v38 > sum){
                    v38 = sum;
                    d38 = d48;
                }
                sum = v27 + p38;
                if(v38 > sum){
                    v38 = sum;
                    d38 = d27;
                }
            }
            if(rc.onTheMap(l70)){
                p70 = 20*(rc.senseRubble(l70)/10 + 1);
                sum = v61 + p70;
                if(v70 > sum){
                    v70 = sum;
                    d70 = d61;
                }
                sum = v71 + p70;
                if(v70 > sum){
                    v70 = sum;
                    d70 = d71;
                }
                sum = v60 + p70;
                if(v70 > sum){
                    v70 = sum;
                    d70 = d60;
                }
                sum = v81 + p70;
                if(v70 > sum){
                    v70 = sum;
                    d70 = d81;
                }
            }
            if(rc.onTheMap(l78)){
                p78 = 20*(rc.senseRubble(l78)/10 + 1);
                sum = v67 + p78;
                if(v78 > sum){
                    v78 = sum;
                    d78 = d67;
                }
                sum = v77 + p78;
                if(v78 > sum){
                    v78 = sum;
                    d78 = d77;
                }
                sum = v68 + p78;
                if(v78 > sum){
                    v78 = sum;
                    d78 = d68;
                }
                sum = v87 + p78;
                if(v78 > sum){
                    v78 = sum;
                    d78 = d87;
                }
            }
            System.out.println("Ran BFS: " + Clock.getBytecodesLeft());
            int dx = target.x - l54.x;
            int dy = target.y - l54.y;
            switch (dx) {
                case -4:
                    switch (dy){
                        case -2:
                            return d12;
                        case -1:
                            return d13;
                        case 0:
                            return d14;
                        case 1:
                            return d15;
                        case 2:
                            return d16;
                    }
                    break;
                case -3:
                    switch (dy){
                        case -3:
                            return d21;
                        case -2:
                            return d22;
                        case -1:
                            return d23;
                        case 0:
                            return d24;
                        case 1:
                            return d25;
                        case 2:
                            return d26;
                        case 3:
                            return d27;
                    }
                    break;
                case -2:
                    switch (dy){
                        case -4:
                            return d30;
                        case -3:
                            return d31;
                        case -2:
                            return d32;
                        case -1:
                            return d33;
                        case 0:
                            return d34;
                        case 1:
                            return d35;
                        case 2:
                            return d36;
                        case 3:
                            return d37;
                        case 4:
                            return d38;
                    }
                    break;
                case -1:
                    switch (dy){
                        case -4:
                            return d40;
                        case -3:
                            return d41;
                        case -2:
                            return d42;
                        case -1:
                            return d43;
                        case 0:
                            return d44;
                        case 1:
                            return d45;
                        case 2:
                            return d46;
                        case 3:
                            return d47;
                        case 4:
                            return d48;
                    }
                    break;
                case 0:
                    switch (dy){
                        case -4:
                            return d50;
                        case -3:
                            return d51;
                        case -2:
                            return d52;
                        case -1:
                            return d53;
                        case 0:
                            return d54;
                        case 1:
                            return d55;
                        case 2:
                            return d56;
                        case 3:
                            return d57;
                        case 4:
                            return d58;
                    }
                    break;
                case 1:
                    switch (dy){
                        case -4:
                            return d60;
                        case -3:
                            return d61;
                        case -2:
                            return d62;
                        case -1:
                            return d63;
                        case 0:
                            return d64;
                        case 1:
                            return d65;
                        case 2:
                            return d66;
                        case 3:
                            return d67;
                        case 4:
                            return d68;
                    }
                    break;
                case 2:
                    switch (dy){
                        case -4:
                            return d70;
                        case -3:
                            return d71;
                        case -2:
                            return d72;
                        case -1:
                            return d73;
                        case 0:
                            return d74;
                        case 1:
                            return d75;
                        case 2:
                            return d76;
                        case 3:
                            return d77;
                        case 4:
                            return d78;
                    }
                    break;
                case 3:
                    switch (dy){
                        case -3:
                            return d81;
                        case -2:
                            return d82;
                        case -1:
                            return d83;
                        case 0:
                            return d84;
                        case 1:
                            return d85;
                        case 2:
                            return d86;
                        case 3:
                            return d87;
                    }
                    break;
                case 4:
                    switch (dy){
                        case -2:
                            return d92;
                        case -1:
                            return d93;
                        case 0:
                            return d94;
                        case 1:
                            return d95;
                        case 2:
                            return d96;
                    }
                    break;
            }
            System.out.println("Didn't find within radius, gonna use distance heuristic: " + Clock.getBytecodesLeft());
            Direction ans = null;
            double bestScore = Double.MAX_VALUE;
            double currScore;
            double tempDist;
            tempDist = v22/10;
            currScore = l22.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d22;
                System.out.println("Best end location: " + bestScore + ", " + l22.toString());}
            tempDist = v26/10;
            currScore = l26.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d26;
                System.out.println("Best end location: " + bestScore + ", " + l26.toString());}
            tempDist = v82/10;
            currScore = l82.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d82;
                System.out.println("Best end location: " + bestScore + ", " + l82.toString());}
            tempDist = v86/10;
            currScore = l86.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d86;
                System.out.println("Best end location: " + bestScore + ", " + l86.toString());}
            tempDist = v31/10;
            currScore = l31.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d31;
                System.out.println("Best end location: " + bestScore + ", " + l31.toString());}
            tempDist = v37/10;
            currScore = l37.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d37;
                System.out.println("Best end location: " + bestScore + ", " + l37.toString());}
            tempDist = v71/10;
            currScore = l71.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d71;
                System.out.println("Best end location: " + bestScore + ", " + l71.toString());}
            tempDist = v77/10;
            currScore = l77.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d77;
                System.out.println("Best end location: " + bestScore + ", " + l77.toString());}
            tempDist = v21/10;
            currScore = l21.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d21;
                System.out.println("Best end location: " + bestScore + ", " + l21.toString());}
            tempDist = v27/10;
            currScore = l27.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d27;
                System.out.println("Best end location: " + bestScore + ", " + l27.toString());}
            tempDist = v81/10;
            currScore = l81.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d81;
                System.out.println("Best end location: " + bestScore + ", " + l81.toString());}
            tempDist = v87/10;
            currScore = l87.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d87;
                System.out.println("Best end location: " + bestScore + ", " + l87.toString());}
            tempDist = v14/10;
            currScore = l14.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d14;
                System.out.println("Best end location: " + bestScore + ", " + l14.toString());}
            tempDist = v94/10;
            currScore = l94.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d94;
                System.out.println("Best end location: " + bestScore + ", " + l94.toString());}
            tempDist = v50/10;
            currScore = l50.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d50;
                System.out.println("Best end location: " + bestScore + ", " + l50.toString());}
            tempDist = v58/10;
            currScore = l58.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d58;
                System.out.println("Best end location: " + bestScore + ", " + l58.toString());}
            tempDist = v13/10;
            currScore = l13.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d13;
                System.out.println("Best end location: " + bestScore + ", " + l13.toString());}
            tempDist = v15/10;
            currScore = l15.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d15;
                System.out.println("Best end location: " + bestScore + ", " + l15.toString());}
            tempDist = v93/10;
            currScore = l93.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d93;
                System.out.println("Best end location: " + bestScore + ", " + l93.toString());}
            tempDist = v95/10;
            currScore = l95.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d95;
                System.out.println("Best end location: " + bestScore + ", " + l95.toString());}
            tempDist = v40/10;
            currScore = l40.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d40;
                System.out.println("Best end location: " + bestScore + ", " + l40.toString());}
            tempDist = v48/10;
            currScore = l48.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d48;
                System.out.println("Best end location: " + bestScore + ", " + l48.toString());}
            tempDist = v60/10;
            currScore = l60.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d60;
                System.out.println("Best end location: " + bestScore + ", " + l60.toString());}
            tempDist = v68/10;
            currScore = l68.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d68;
                System.out.println("Best end location: " + bestScore + ", " + l68.toString());}
            tempDist = v12/10;
            currScore = l12.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d12;
                System.out.println("Best end location: " + bestScore + ", " + l12.toString());}
            tempDist = v16/10;
            currScore = l16.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d16;
                System.out.println("Best end location: " + bestScore + ", " + l16.toString());}
            tempDist = v92/10;
            currScore = l92.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d92;
                System.out.println("Best end location: " + bestScore + ", " + l92.toString());}
            tempDist = v96/10;
            currScore = l96.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d96;
                System.out.println("Best end location: " + bestScore + ", " + l96.toString());}
            tempDist = v30/10;
            currScore = l30.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d30;
                System.out.println("Best end location: " + bestScore + ", " + l30.toString());}
            tempDist = v38/10;
            currScore = l38.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d38;
                System.out.println("Best end location: " + bestScore + ", " + l38.toString());}
            tempDist = v70/10;
            currScore = l70.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d70;
                System.out.println("Best end location: " + bestScore + ", " + l70.toString());}
            tempDist = v78/10;
            currScore = l78.distanceSquaredTo(target) + tempDist*tempDist;
            if(currScore < bestScore){
                bestScore = currScore;
                ans = d78;
                System.out.println("Best end location: " + bestScore + ", " + l78.toString());}
            return ans;
        } catch (Exception e){
            e.printStackTrace();
        }return null;
    }

    public Direction getBestDir(MapLocation target) throws GameActionException {
        Direction targetDir = robot.myLoc.directionTo(target);
//        switch(targetDir){
//            case NORTH:
//                return runBFSNorth(target);
//            case SOUTH:
//                return runBFSSouth(target);
//            case EAST:
//                return runBFSEast(target);
//            case WEST:
//                return runBFSWest(target);
//            case NORTHEAST:
//                return runBFSNortheast(target);
//            case NORTHWEST:
//                return runBFSNorthwest(target);
//            case SOUTHEAST:
//                return runBFSSoutheast(target);
//            case SOUTHWEST:
//                return runBFSSouthwest(target);
//        }
        return runBFS(target);
//        System.out.println("ERROR DIRECTION UNKNOWN");
//        return null;
    }
}

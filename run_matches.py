import subprocess

def parse_winner(str, p1, p2):
    ls = str.splitlines()
    for line in ls[-50:]: # save some looping time
        if "wins" in line:
            if len(p2) > len(p1):
                if p2 in line:
                    return p2
                else:
                    return p1
            else:
                if p1 in line:
                    return p1
                else:
                    return p2

def main():

    ## Fill in desired players and maps here!
    players = ["sprintbot", "sprintbot2"] # Bot names (i.e. examplefuncsplayer, should be folders in src/ directory)
    maps = ["eckleburg", "intersection", "maptestsmall"] # Maps
    scaffold_directory = "./" # Battlefold Scaffold location ("./" if this file is in scaffold location)
    ##

    results = {}
    winCount = {}

    for i in range(len(players)):
        player = players[i]
        results[player] = {}
        if player not in winCount:
            winCount[player] = 0

        print(player + ": ")
        for j in range(len(players)):
            if (i == j):
                continue
            
            opponent = players[j]
            if opponent not in winCount:
                winCount[opponent] = 0

            for map in maps:          
                match_result = subprocess.check_output(
                    ["gradlew", "run",
                    f"-PteamA={player}", f"-PpackageNameA={player}",
                    f"-PteamB={opponent}", f"-PpackageNameB={opponent}",
                    f"-Pmaps={map}"],
                    cwd=scaffold_directory,
                    shell=True
                    ).decode('UTF-8')

                winner = parse_winner(match_result, player, opponent)

                results[player][opponent] = winner
                winCount[winner] += 1

                print(f"{player} ({winCount[player]}) - {opponent} ({winCount[opponent]}) [{map}]: {winner}")
        print()

    print("Scoreboard: ")
    for k,v in winCount.items():
        print(f"{k}: {v}/{2*(len(players)-1) * len(maps)}")

if __name__ == "__main__":
    main()
import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

import BigBoard, { Player } from './bigBoard';
import PlayerList from './playerList';
import BoardParameters from './boardParametes';
import ContextMenu from './ContextMenu';
import AddPlayerModal from './AddPlayerModal';
import BubbleMenu from './BubbleMenu';

import './maincomponent.scss';

const MainComponent: React.FC = () => {

    const navigate = useNavigate();
    const location = useLocation();

    const params = new URLSearchParams(location.search);
    const teamsFromURL = Number(params.get('teams'));
    const roundsFromURL = Number(params.get('rounds'));
    const filterIdFromURL = params.get('filterId');


    const hasValidParams = (teamsFromURL > 0 && teamsFromURL <= 16) && (roundsFromURL > 0 && roundsFromURL <= 15);


    const [teams, setTeams] = useState<number>(teamsFromURL || 12);
    const [rounds, setRounds] = useState<number>(roundsFromURL || 3);
    const [players, setPlayers] = useState<(Player | null)[][]>(
        hasValidParams
            ? Array.from({ length: roundsFromURL }, () => Array(teamsFromURL).fill(null))
            : []
    );
    const [isGridCreated, setIsGridCreated] = useState<boolean>(hasValidParams);

    const [playerListOpen, setPlayerListOpen] = useState<boolean>(true);

    const [playerPool, setPlayerPool] = useState<Player[]>([]);
    const [tierBreaks, setTierBreaks] = useState<{ row: number; col: number }[]>([]);

    const [menuVisible, setMenuVisible] = useState(false);
    const [menuPosition, setMenuPosition] = useState({ x: 0, y: 0 });
    const [showAddPlayerModal, setShowAddPlayerModal] = useState(false);

    useEffect(() => {
        if (hasValidParams) {
            setPlayers(Array.from({ length: roundsFromURL }, () => Array(teamsFromURL).fill(null)));
        }
    }, [hasValidParams, roundsFromURL, teamsFromURL]);


    /*
    Logic that makes a call to update ADP when its filled out 
    */
    useEffect(() => {
        const allFilled = players.length > 0 && players.every(row => row.every(cell => cell !== null));

        if (allFilled) {
            const flatPlayers = players.flat().filter((p): p is Player => p !== null);
            const draftType = filterIdFromURL ? "filtered" : "offline";

            fetch(`/api/draft/complete?draftType=${draftType}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(flatPlayers),
            })
                .then(async res => {
                    if (!res.ok) throw new Error("Failed to submit draft.");
                    const uuid = await res.text();
                    navigate(`/draft/${uuid}`);
                })
                .catch(err => console.error("Error submitting draft:", err));

        }
    }, [players]);


    const resetDraft = () => {
        setTeams(12);
        setRounds(3);
        setPlayers([]);
        setIsGridCreated(false);
        setTierBreaks([]);
        setMenuVisible(false);
        setShowAddPlayerModal(false);
    };


    const handleContextMenu = (e: React.MouseEvent) => {
        e.preventDefault();
        setMenuPosition({ x: e.pageX, y: e.pageY });
        setMenuVisible(true);
    };

    const addTierBreakAfterLastPick = () => {
        for (let r = rounds - 1; r >= 0; r--) {
            for (let c = teams - 1; c >= 0; c--) {
                if (players[r][c]) {
                    const newBreak = { row: r + 1, col: c + 1 };
                    const alreadyExists = tierBreaks.some(tb => tb.row === newBreak.row && tb.col === newBreak.col);
                    if (!alreadyExists) {
                        setTierBreaks((prev) => [...prev, newBreak]);
                    }
                    return;
                }
            }
        }
    };

    const removeLastTierBreak = () => {
        setTierBreaks((prev) => prev.slice(0, -1));
    };

    const clearTierBreaks = () => setTierBreaks([]);

    const addNewPlayer = (player: Player) => {
        setPlayerPool((prev) => [player, ...prev]);
    };

    const setDefaultPlayerPool = () => {
        fetch("/api/players")
            .then((res) => res.json())
            .then((data: Player[]) => setPlayerPool(data))
            .catch((err) => console.error("Failed to fetch players:", err));
    };

    useEffect(() => {
        if (filterIdFromURL) {
            fetch(`/api/players/filter/${filterIdFromURL}`)
                .then((res) => res.json())
                .then((data: Player[]) => setPlayerPool(data))
                .catch((err) => console.error("Failed to fetch players with filter:", err));
        } else {
            setDefaultPlayerPool();
        }
    }, [filterIdFromURL]);


    const createGrid = () => {
        const searchParams = new URLSearchParams(location.search);

        searchParams.set('teams', teams.toString());
        searchParams.set('rounds', rounds.toString());

        navigate({ pathname: location.pathname, search: searchParams.toString() });

        setPlayers(Array.from({ length: rounds }, () => Array(teams).fill(null)));
        setIsGridCreated(true);
    };



    const addPlayerToNextOpenSpot = (player: Player) => {
        setPlayers((prevPlayers) => {
            const updatedPlayers = prevPlayers.map((rowArr) => [...rowArr]);

            for (let r = 0; r < rounds; r++) {
                for (let c = 0; c < teams; c++) {
                    if (!updatedPlayers[r][c]) {
                        updatedPlayers[r][c] = player;
                        setPlayerPool((prevPool) => prevPool.filter((p) => p.name !== player.name));
                        return updatedPlayers;
                    }
                }
            }
            return prevPlayers;
        });
    };

    const removeDraftedPlayer = (row: number, col: number) => {
        setPlayers((prevPlayers) => {
            const updatedPlayers = prevPlayers.map((rowArr) => [...rowArr]);
            const playerToRemove = updatedPlayers[row - 1][col - 1];
            if (playerToRemove) {
                updatedPlayers[row - 1][col - 1] = null;
                setPlayerPool((prevPool) => [...prevPool, playerToRemove]);
            }
            return updatedPlayers;
        });
    };

    const removeLastPick = () => {
        setPlayers((prevPlayers) => {
            const updatedPlayers = prevPlayers.map((rowArr) => [...rowArr]);

            for (let r = rounds - 1; r >= 0; r--) {
                for (let c = teams - 1; c >= 0; c--) {
                    if (updatedPlayers[r][c]) {
                        const removedPlayer = updatedPlayers[r][c];
                        updatedPlayers[r][c] = null;

                        if (removedPlayer) {
                            setPlayerPool((prevPool) => [...prevPool, removedPlayer]);
                        }

                        return updatedPlayers;
                    }
                }
            }

            return prevPlayers;
        });
    };

    const clearBoard = () => {
        setDefaultPlayerPool();
        setPlayers(Array.from({ length: rounds }, () => Array(teams).fill(null)));
    };

    const exportDraft = () => {
        let draftText = "<h2>Rookie Fantasy Football Draft Order:</h2><ul>";

        players.forEach((row, rIndex) => {
            if (rIndex > 0) {
                draftText += "<br>";
            }
            row.forEach((player, cIndex) => {
                const round = rIndex + 1;
                const pick = cIndex + 1;
                draftText += `<li>${round}.${pick.toString().padStart(2, '0')} ${player ? player.name : '---'}</li>`;
            });
        });

        draftText += "</ul>";

        const printWindow = window.open('', '_blank');
        if (printWindow) {
            printWindow.document.write(`
                <html>
                <head>
                    <title>Draft Order</title>
                    <style>
                        body { font-family: Arial, sans-serif; padding: 20px; }
                        h2 { text-align: center; margin-top:0px;}
                        ul { list-style-type: none; padding: 0; }
                        li { font-size: 18px; margin: 5px 0; }
                        @media print {
                            @page { margin: 0; }
                            body { margin: 1in; }
                        }
                    </style>
                </head>
                <body>
                    ${draftText}
                </body>
                </html>
            `);
            printWindow.document.close();
            printWindow.print();
        }
    };

    const isBoardPopulated = players.some((row) => row.some((cell) => cell !== null));

    return (
        <div className={`main-component ${!isGridCreated ? 'center-content' : ''}`}>
            {!isGridCreated ? (
                <BoardParameters
                    teams={teams}
                    rounds={rounds}
                    handleTeamsChange={(e) => setTeams(Number(e.target.value))}
                    handleRoundsChange={(e) => setRounds(Number(e.target.value))}
                    createGrid={createGrid}
                />
            ) : (
                <div className="board-container" onContextMenu={handleContextMenu}>
                    <ContextMenu
                        visible={menuVisible}
                        position={menuPosition}
                        onClose={() => setMenuVisible(false)}
                        onAddTierBreak={addTierBreakAfterLastPick}
                        onRemoveLastTierBreak={removeLastTierBreak}
                        onResetDraft={resetDraft}
                        onClearBoard={() => {
                            clearBoard();
                            setMenuVisible(false);
                        }}
                        onExportDraft={() => {
                            exportDraft();
                            setMenuVisible(false);
                        }}
                        onLastPlayerRemove={() => {
                            removeLastPick();
                            setMenuVisible(false);
                        }}
                        onAddPlayerClick={() => {
                            setMenuVisible(false);
                            setShowAddPlayerModal(true);
                        }}
                        isBoardPopulated={isBoardPopulated}
                    />
                    <BubbleMenu
                        onClearBoard={clearBoard}
                        onExportDraft={exportDraft}
                        onLastPlayerRemove={removeLastPick}
                        onAddPlayerClick={() => {
                            setShowAddPlayerModal(true);
                        }}
                        onAddTierBreak={addTierBreakAfterLastPick}
                        onRemoveLastTierBreak={removeLastTierBreak}
                        onResetDraft={resetDraft}
                        isBoardPopulated={isBoardPopulated}
                        playerListOpen={playerListOpen}
                        setPlayerListOpen={setPlayerListOpen}
                    />


                    <AddPlayerModal
                        visible={showAddPlayerModal}
                        onClose={() => setShowAddPlayerModal(false)}
                        onSubmit={addNewPlayer}
                    />
                    <PlayerList
                        playerPool={playerPool}
                        addPlayerToNextOpenSpot={addPlayerToNextOpenSpot}
                        playerListOpen={playerListOpen}
                    />

                    <BigBoard
                        rounds={rounds}
                        teams={teams}
                        players={players}
                        removeDraftedPlayer={removeDraftedPlayer}
                        tierBreaks={tierBreaks}
                    />
                </div>
            )}
        </div>
    );
};

export default MainComponent;
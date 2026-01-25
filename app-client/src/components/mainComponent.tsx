import React, { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

import BigBoard, { Player } from './draft/BigBoard';
import MobileDraft from './mobile/MobileDraft';
import PlayerList from './draft/PlayerList';
import BoardParameters from './draft/BoardParameters';
import BubbleMenu from './shared/BubbleMenu';

import './maincomponent.scss';

const MainComponent: React.FC = () => {

    const navigate = useNavigate();
    const location = useLocation();

    const params = new URLSearchParams(location.search);
    const teamsFromURL = Number(params.get('teams'));
    const roundsFromURL = Number(params.get('rounds'));
    const rookiesOnlyFromURL = params.get('rookiesOnly') === 'true';

    const hasValidParams = (teamsFromURL > 0 && teamsFromURL <= 16) && (roundsFromURL > 0 && roundsFromURL <= 15);

    const [teams, setTeams] = useState<number>(teamsFromURL || 12);
    const [rounds, setRounds] = useState<number>(roundsFromURL || 3);
    const [rookiesOnly, setRookiesOnly] = useState<boolean>(rookiesOnlyFromURL || false);
    const [players, setPlayers] = useState<(Player | null)[][]>(
        hasValidParams
            ? Array.from({ length: roundsFromURL }, () => Array(teamsFromURL).fill(null))
            : []
    );
    const [isGridCreated, setIsGridCreated] = useState<boolean>(hasValidParams);

    const [playerListOpen, setPlayerListOpen] = useState<boolean>(true);
    const [activePositionFilters, setActivePositionFilters] = useState<string[]>([]);
    const [activeYearFilters, setActiveYearFilters] = useState<number[]>([]);

    const [playerPool, setPlayerPool] = useState<Player[]>([]);
    const [tierBreaks, setTierBreaks] = useState<{ row: number; col: number }[]>([]);
    const [isMobile, setIsMobile] = useState<boolean>(window.innerWidth <= 768);

    const currentYear = new Date().getFullYear();
    const yearRange = Array.from({ length: 4 }, (_, i) => currentYear + i);

    useEffect(() => {
        const handleResize = () => {
            setIsMobile(window.innerWidth <= 768);
        };
        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, []);

    const togglePositionFilter = (position: string) => {
        if (position === 'ALL') {
            setActivePositionFilters([]);
        } else {
            setActivePositionFilters((prev) =>
                prev.includes(position) ? prev.filter((p) => p !== position) : [...prev, position]
            );
        }
    };

    const toggleYearFilter = (year: number) => {
        setActiveYearFilters((prev) =>
            prev.includes(year) ? prev.filter((y) => y !== year) : [...prev, year]
        );
    };

    useEffect(() => {
        if (hasValidParams) {
            setPlayers(Array.from({ length: roundsFromURL }, () => Array(teamsFromURL).fill(null)));
        }
    }, [hasValidParams, roundsFromURL, teamsFromURL]);

    /*
    Logic that makes a call to save the draft when it's filled out 
    */
    useEffect(() => {
        const allFilled = players.length > 0 && players.every(row => row.every(cell => cell !== null));

        if (allFilled) {
            const flatPlayers = players.flat().filter((p): p is Player => p !== null);
            
            // Check if all players have IDs
            const playersWithoutIds = flatPlayers.filter(p => !p.id);
            if (playersWithoutIds.length > 0) {
                console.error('Some players are missing IDs:', playersWithoutIds);
                alert('Error: Some players are missing IDs. Cannot save draft.');
                return;
            }
            
            // Prepare draft data for the new API
            const draftData = {
                draftName: `${teams}-Team ${rounds}-Round Draft - ${new Date().toLocaleDateString()}`,
                participantCount: teams,
                picks: flatPlayers.map((player, index) => ({
                    playerId: player.id!,
                    pickNumber: index + 1
                }))
            };

            console.log('Saving draft with data:', draftData);

            fetch(`/api/drafts`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(draftData),
            })
                .then(async res => {
                    if (!res.ok) {
                        const errorText = await res.text();
                        console.error('Server error response:', errorText);
                        throw new Error(`Failed to save draft: ${errorText}`);
                    }
                    const response = await res.json();
                    console.log('Draft saved successfully:', response);
                    alert(`Draft saved! UUID: ${response.uuid}`);
                })
                .catch(err => {
                    console.error("Error saving draft:", err);
                    alert(`Failed to save draft: ${err.message}`);
                });

        }
    }, [players, teams, rounds]);

    const resetDraft = () => {
        setTeams(12);
        setRounds(3);
        setPlayers([]);
        setIsGridCreated(false);
        setTierBreaks([]);
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

    const loadPlayerPool = () => {
        fetch("/api/players")
            .then((res) => res.json())
            .then((data: Player[]) => {
                // Filter to current year only if rookiesOnly is enabled
                const filteredData = rookiesOnly 
                    ? data.filter(player => player.draftyear === currentYear)
                    : data;
                setPlayerPool(filteredData);
            })
            .catch((err) => console.error("Failed to fetch players:", err));
    };

    useEffect(() => {
        loadPlayerPool();
    }, [rookiesOnly]);

    const createGrid = () => {
        const searchParams = new URLSearchParams(location.search);

        searchParams.set('teams', teams.toString());
        searchParams.set('rounds', rounds.toString());
        searchParams.set('rookiesOnly', rookiesOnly.toString());

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

    const addPlayerToSpot = (player: Player, row: number, col: number) => {
        setPlayers((prevPlayers) => {
            const updatedPlayers = prevPlayers.map((rowArr) => [...rowArr]);
            updatedPlayers[row - 1][col - 1] = player;
            setPlayerPool((prevPool) => prevPool.filter((p) => p.name !== player.name));
            return updatedPlayers;
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
        loadPlayerPool();
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
    const isDraftComplete = players.length > 0 && players.every(row => row.every(cell => cell !== null));

    const handleExportDraft = () => {
        // Placeholder for future export functionality
        console.log('Export draft clicked');
    };

    return (
        <div className={`main-component ${!isGridCreated ? 'center-content' : ''}`}>
            {!isGridCreated ? (
                <BoardParameters
                    teams={teams}
                    rounds={rounds}
                    rookiesOnly={rookiesOnly}
                    handleTeamsChange={(e) => setTeams(Number(e.target.value))}
                    handleRoundsChange={(e) => setRounds(Number(e.target.value))}
                    handleRookiesOnlyChange={(e) => setRookiesOnly(e.target.checked)}
                    createGrid={createGrid}
                />
            ) : (
                <div className="board-container">
                    {!isMobile && (
                        <BubbleMenu
                            onClearBoard={clearBoard}
                            onExportDraft={exportDraft}
                            onLastPlayerRemove={removeLastPick}
                            onAddPlayerClick={() => {}}
                            onAddTierBreak={addTierBreakAfterLastPick}
                            onRemoveLastTierBreak={removeLastTierBreak}
                            onResetDraft={resetDraft}
                            isBoardPopulated={isBoardPopulated}
                            playerListOpen={playerListOpen}
                            setPlayerListOpen={setPlayerListOpen}
                        />
                    )}
                    
                    {!isMobile && playerListOpen && (
                        <div className="filter-toolbar">
                            <div className="filter-section">
                                <span className="filter-label">Position:</span>
                                <div className="filter-buttons">
                                    <button
                                        className={`filter-btn ${activePositionFilters.length === 0 ? 'active' : ''}`}
                                        onClick={() => togglePositionFilter('ALL')}
                                    >
                                        All
                                    </button>
                                    {['QB', 'RB', 'WR', 'TE'].map((position) => (
                                        <button
                                            key={position}
                                            className={`filter-btn ${activePositionFilters.includes(position) ? 'active' : ''}`}
                                            onClick={() => togglePositionFilter(position)}
                                        >
                                            {position}
                                        </button>
                                    ))}
                                </div>
                            </div>
                            {!rookiesOnly && (
                                <div className="filter-section">
                                    <span className="filter-label">Draft Class:</span>
                                    <div className="filter-buttons">
                                        {yearRange.map((year) => (
                                            <button
                                                key={year}
                                                className={`filter-btn ${activeYearFilters.includes(year) ? 'active' : ''}`}
                                                onClick={() => toggleYearFilter(year)}
                                            >
                                                {year}
                                            </button>
                                        ))}
                                    </div>
                                </div>
                            )}
                            <div className="helper-section">
                                <button 
                                    className="helper-btn" 
                                    onClick={clearBoard}
                                    disabled={!isBoardPopulated}
                                    title="Clear Board"
                                >
                                    ❌
                                </button>
                                <button 
                                    className="helper-btn" 
                                    onClick={removeLastPick}
                                    disabled={!isBoardPopulated}
                                    title="Undo Last Pick"
                                >
                                    ↩️
                                </button>
                                <div className="tier-break-group">
                                    <span className="helper-label">Tier Break:</span>
                                    <button 
                                        className="helper-btn" 
                                        onClick={addTierBreakAfterLastPick}
                                        disabled={!isBoardPopulated}
                                        title="Add Tier Break"
                                    >
                                        ➕
                                    </button>
                                    <button 
                                        className="helper-btn" 
                                        onClick={removeLastTierBreak}
                                        title="Remove Last Tier Break"
                                    >
                                        ➖
                                    </button>
                                </div>
                            </div>
                            <div className="actions-section">
                                {isDraftComplete && (
                                    <button className="action-btn export-btn" onClick={handleExportDraft}>
                                        📋 Export Draft
                                    </button>
                                )}
                                <button className="action-btn exit-btn" onClick={resetDraft}>
                                    🚪 Exit Draft
                                </button>
                            </div>
                        </div>
                    )}
                    
                    {isMobile ? (
                        <MobileDraft
                            teams={teams}
                            rounds={rounds}
                            players={players}
                            playerPool={playerPool}
                            onDraftPlayer={addPlayerToSpot}
                            onRemovePlayer={removeDraftedPlayer}
                            onExport={exportDraft}
                            onExit={() => navigate('/')}
                            rookiesOnly={rookiesOnly}
                        />
                    ) : (
                        <div className="board-content-wrapper">
                            <PlayerList
                                playerPool={playerPool}
                                addPlayerToNextOpenSpot={addPlayerToNextOpenSpot}
                                playerListOpen={playerListOpen}
                                activePositionFilters={activePositionFilters}
                                activeYearFilters={activeYearFilters}
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
            )}
        </div>
    );
};

export default MainComponent;
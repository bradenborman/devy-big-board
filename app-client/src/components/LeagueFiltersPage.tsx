import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import './league-filters.scss';
import { useNavigate } from 'react-router-dom';

interface LeagueFilter {
    id: number;
    leagueName: string;
    createdAt: string;
}

interface Player {
    name: string;
    position: string;
    team: string;
    draftyear: number;
}

const LeagueFiltersPage: React.FC = () => {
    const navigate = useNavigate();


    const [filters, setFilters] = useState<LeagueFilter[]>([]);
    const [newLeagueName, setNewLeagueName] = useState('');
    const [allPlayers, setAllPlayers] = useState<Player[]>([]);
    const [selectedPlayers, setSelectedPlayers] = useState<string[]>([]);
    const [showInfo, setShowInfo] = useState(false);

    useEffect(() => {
        fetch('/api/filters')
            .then(res => res.json())
            .then(data => setFilters(data))
            .catch(err => console.error('Failed to fetch filters:', err));

        fetch('/api/players')
            .then(res => res.json())
            .then(data => setAllPlayers(data))
            .catch(err => console.error('Failed to load players:', err));
    }, []);

    const startDraftWithFilter = (filterId: number) => {
        navigate(`/?filterId=${filterId}`);
    };


    const togglePlayer = (playerKey: string) => {
        setSelectedPlayers(prev =>
            prev.includes(playerKey)
                ? prev.filter(p => p !== playerKey)
                : [...prev, playerKey]
        );
    };

    const createNewFilter = () => {
        if (!newLeagueName.trim() || selectedPlayers.length === 0) return;

        fetch('/api/filters', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newLeagueName)
        })
            .then(res => res.json())
            .then(filterId => {
                const playerRequests = selectedPlayers.map(playerStr => {
                    const [name, position, team] = playerStr.split('|');
                    return fetch(`/api/filters/${filterId}/add`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ name, position, team, draftyear: 0 })
                    });
                });

                return Promise.all(playerRequests).then(() => filterId);
            })
            .then(filterId => {
                setFilters(prev => [...prev, { id: filterId, leagueName: newLeagueName, createdAt: new Date().toISOString() }]);
                setNewLeagueName('');
                setSelectedPlayers([]);
            })
            .catch(err => console.error('Failed to create filter:', err));
    };

    const deleteFilter = (filterId: number) => {
        if (!window.confirm('Are you sure you want to delete this filter?')) return;

        fetch(`/api/filters/${filterId}`, {
            method: 'DELETE'
        })
            .then(() => {
                setFilters(prev => prev.filter(f => f.id !== filterId));
            })
            .catch(err => console.error('Failed to delete filter:', err));
    };


    return (
        <div className="league-filters-page">
            {/* Top collapsible panel */}
            <div className="info-panel">
                <div className="info-header" onClick={() => setShowInfo(prev => !prev)}>
                    <h2>What are Filters?</h2>
                    <span>{showInfo ? '▲' : '▼'}</span>
                </div>
                {showInfo && (
                    <div className="info-content">
                        <p>Filters allow you to exclude players from your draft pool for specific leagues.
                            Define a league name and select players who are already owned or unavailable.
                            You must select at least one player to create a filter.
                        </p>
                    </div>
                )}
            </div>

            {/* New 2-column flex layout */}
            <div className="filters-grid">
                {/* Left column: Create Filter */}
                <div className="create-filter-form">
                    <h2>Create New League Filter</h2>
                    <input
                        type="text"
                        placeholder="New League Name"
                        value={newLeagueName}
                        onChange={e => setNewLeagueName(e.target.value)}
                    />
                    <div className="player-checkboxes">
                        {allPlayers.map(player => {
                            const key = `${player.name}|${player.position}|${player.team}`;
                            return (
                                <label key={key} className="player-checkbox">
                                    <input
                                        type="checkbox"
                                        checked={selectedPlayers.includes(key)}
                                        onChange={() => togglePlayer(key)}
                                    />
                                    {player.name} - {player.position} - {player.team}
                                </label>
                            );
                        })}
                    </div>
                    <button
                        onClick={createNewFilter}
                        disabled={!newLeagueName.trim() || selectedPlayers.length === 0}
                    >
                        Create Filter
                    </button>
                </div>

                {/* Right column: Table of Existing Filters */}
                <div className="existing-filters">
                    <h2>Existing Filters</h2>
                    <table>
                        <thead>
                            <tr>
                                <th>League Name</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filters.map(filter => (
                                <tr key={filter.id}>
                                    <td>{filter.leagueName}</td>
                                    <td>
                                        <button className="edit-btn" disabled>Edit</button>
                                        <button
                                            className="delete-btn"
                                            onClick={() => deleteFilter(filter.id)}
                                        >
                                            Delete
                                        </button>
                                        <button
                                            className="start-draft-btn"
                                            onClick={() => startDraftWithFilter(filter.id)}
                                        >
                                            Start Draft
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

            </div>

            <div style={{ marginTop: '20px' }}>
                <Link to="/">Back to Draft Board</Link>
            </div>
        </div>
    );
};

export default LeagueFiltersPage;

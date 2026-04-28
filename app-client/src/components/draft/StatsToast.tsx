import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { Player } from './BigBoard';

interface StatMap { [statType: string]: string; }
interface StatsResponse {
    season: number;
    team: string;
    position: string;
    stats: { [category: string]: StatMap };
}

interface StatsToastProps {
    player: Player | null;
    position: 'center' | 'right';
}

const CATEGORY_LABELS: Record<string, string> = {
    passing: 'Passing', rushing: 'Rushing', receiving: 'Receiving',
};

const STAT_LABELS: Record<string, string> = {
    ATT: 'Att', COMPLETIONS: 'Cmp', PCT: 'Pct', YDS: 'Yds',
    TD: 'TD', INT: 'Int', YPA: 'Y/A', CAR: 'Car', YPC: 'Y/C',
    REC: 'Rec', YPR: 'Y/R', LONG: 'Lng',
};

const StatsToast: React.FC<StatsToastProps> = ({ player, position }) => {
    const [stats, setStats] = useState<StatsResponse | null>(null);
    const [visible, setVisible] = useState(false);
    const [animating, setAnimating] = useState(false);

    useEffect(() => {
        if (!player?.id) return;

        setStats(null);
        setAnimating(true);
        setVisible(true);

        fetch(`/api/players/${player.id}/stats`)
            .then(res => res.status === 204 ? null : res.json())
            .then(data => { setStats(data); setAnimating(false); })
            .catch(() => setAnimating(false));
    }, [player?.id]);

    if (!visible || !player) return null;

    const posClass = position === 'center' ? 'stats-toast-center' : 'stats-toast-right';

    const toast = (
        <div className={`stats-toast stats-toast-enter ${posClass}`}>
            <div className="stats-toast-header">
                <span className="stats-toast-name">{player.name}</span>
                <span className={`stats-toast-pos ${player.position}`}>{player.position}</span>
                {stats && <span className="stats-toast-season">{stats.season} · {stats.team}</span>}
            </div>
            {animating && <div className="stats-toast-loading">Loading stats…</div>}
            {!animating && !stats && <div className="stats-toast-loading">No stats available</div>}
            {!animating && stats && Object.entries(stats.stats).map(([category, statMap]) => (
                <div key={category} className="stats-toast-category">
                    <div className="stats-toast-cat-label">{CATEGORY_LABELS[category] ?? category}</div>
                    <div className="stats-toast-grid">
                        {Object.entries(statMap).map(([type, val]) => (
                            <div key={type} className="stats-toast-cell">
                                <span className="stats-toast-val">{val}</span>
                                <span className="stats-toast-key">{STAT_LABELS[type] ?? type}</span>
                            </div>
                        ))}
                    </div>
                </div>
            ))}
        </div>
    );

    return createPortal(toast, document.body);
};

export default StatsToast;

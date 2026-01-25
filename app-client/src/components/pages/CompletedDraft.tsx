import React, { useEffect, useState } from 'react';
import { CompletedDraftResponse, DraftPick } from '../../models/CompletedDraftResponse';

import './completedDraft.scss';

interface CompletedDraftProps {
    uuid: string;
}

const CompletedDraft: React.FC<CompletedDraftProps> = ({ uuid }) => {
    const [draftData, setDraftData] = useState<CompletedDraftResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetch(`/api/draft/${uuid}`)
            .then((res) => {
                if (!res.ok) throw new Error('Failed to fetch draft data');
                return res.json();
            })
            .then((data) => {
                setDraftData(data);
                setLoading(false);
            })
            .catch((err) => {
                console.error(err);
                setError('Error loading draft');
                setLoading(false);
            });
    }, [uuid]);

    if (loading) {
        return <div className="completed-draft">Loading draft...</div>;
    }

    if (error || !draftData) {
        return <div className="completed-draft">Failed to load draft.</div>;
    }

    return (
        <div className="completed-draft">
            <h1>Completed Draft</h1>
            <div className="draft-picks">
                {draftData.picks.length === 0 ? (
                    <p>No picks made yet.</p>
                ) : (
                    <table>
                        <thead>
                            <tr>
                                <th>Pick #</th>
                                <th>Player</th>
                                <th>Position</th>
                                <th>Team</th>
                            </tr>
                        </thead>
                        <tbody>
                            {draftData.picks
                                .sort((a: { pickNumber: number; }, b: { pickNumber: number; }) => a.pickNumber - b.pickNumber)
                                .map((pick: DraftPick) => (
                                    <tr key={pick.pickNumber}>
                                        <td>{pick.pickNumber}</td>
                                        <td>{pick.name}</td>
                                        <td>{pick.position}</td>
                                        <td>{pick.team}</td>
                                    </tr>
                                ))}
                        </tbody>
                    </table>
                )}
            </div>
        </div>
    );
};

export default CompletedDraft;
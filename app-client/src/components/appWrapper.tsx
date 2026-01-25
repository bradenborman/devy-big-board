import React from 'react';
import { BrowserRouter, Routes, Route, useParams } from 'react-router-dom';
import HomePage from './pages/HomePage';
import MainComponent from './mainComponent';
import LiveDraftPage from './pages/LiveDraftPage';
import PlayerManagementPage from './pages/PlayerManagementPage';
import ConsentBanner from './shared/ConsentBanner';
import CompletedDraft from './pages/CompletedDraft';

const AppWrapper: React.FC = () => {
    return (
        <BrowserRouter>
            <ConsentBanner />
            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/offline-draft" element={
                    <div className="app-wrapper">
                        <MainComponent />
                    </div>
                } />
                <Route path="/live-draft" element={<LiveDraftPage />} />
                <Route path="/player-management" element={<PlayerManagementPage />} />
                <Route path="/draft/:uuid" element={<DraftRoute />} />
            </Routes>
        </BrowserRouter>
    );
};

const DraftRoute: React.FC = () => {
    const { uuid } = useParams<{ uuid: string }>();
    if (!uuid) return <div>Invalid Draft</div>;
    return <CompletedDraft uuid={uuid} />
};

export default AppWrapper;
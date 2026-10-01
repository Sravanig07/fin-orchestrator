import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { NotificationItem } from '../types';
import { 
  Bell, 
  Check, 
  Trash2, 
  Plus, 
  Calendar, 
  Clock, 
  AlertTriangle, 
  CreditCard, 
  FileText, 
  RefreshCw,
  X
} from 'lucide-react';

export const NotificationsPage: React.FC = () => {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'ALL' | 'UNREAD' | 'REMINDER' | 'ALERT'>('ALL');
  
  // Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState({
    title: '',
    message: '',
    type: 'REMINDER',
    priority: 'NORMAL',
    triggerTime: new Date(Date.now() + 86400000).toISOString().slice(0, 16)
  });

  const loadNotifications = async () => {
    setLoading(true);
    try {
      const data = await api.getNotifications();
      setNotifications(data);
    } catch (err) {
      console.error('Failed to load notifications:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const handleMarkRead = async (id: number) => {
    try {
      await api.markNotificationRead(id);
      setNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n));
    } catch (err) {
      console.error('Failed to mark notification read:', err);
    }
  };

  const handleCancel = async (id: number) => {
    try {
      await api.cancelReminder(id);
      setNotifications(prev => prev.filter(n => n.id !== id));
    } catch (err) {
      console.error('Failed to cancel reminder:', err);
    }
  };

  const handleCreateReminder = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const newNotif = await api.createReminder({
        ...formData,
        triggerTime: new Date(formData.triggerTime).toISOString()
      });
      setNotifications(prev => [newNotif, ...prev]);
      setIsModalOpen(false);
      setFormData({
        title: '',
        message: '',
        type: 'REMINDER',
        priority: 'NORMAL',
        triggerTime: new Date(Date.now() + 86400000).toISOString().slice(0, 16)
      });
    } catch (err) {
      console.error('Failed to create reminder:', err);
    }
  };

  const filteredNotifications = notifications.filter(item => {
    if (filter === 'UNREAD') return !item.isRead;
    if (filter === 'REMINDER') return item.type === 'REMINDER';
    if (filter === 'ALERT') return item.type === 'PAYMENT_DUE' || item.type === 'CONTRACT_EXPIRY' || item.type === 'RISK_ALERT';
    return true;
  });

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'PAYMENT_DUE':
        return <CreditCard className="w-4 h-4 text-amber-400" />;
      case 'CONTRACT_EXPIRY':
        return <FileText className="w-4 h-4 text-purple-400" />;
      case 'RISK_ALERT':
        return <AlertTriangle className="w-4 h-4 text-red-400" />;
      case 'REMINDER':
      default:
        return <Clock className="w-4 h-4 text-indigo-400" />;
    }
  };

  const getPriorityBadge = (priority: string) => {
    switch (priority) {
      case 'HIGH':
        return <span className="px-2 py-0.5 text-[10px] font-bold bg-red-500/20 text-red-400 border border-red-500/30 rounded">HIGH</span>;
      case 'LOW':
        return <span className="px-2 py-0.5 text-[10px] font-bold bg-slate-800 text-slate-400 border border-slate-700 rounded">LOW</span>;
      case 'NORMAL':
      default:
        return <span className="px-2 py-0.5 text-[10px] font-bold bg-blue-500/20 text-blue-400 border border-blue-500/30 rounded">NORMAL</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-slate-900 p-6 rounded-2xl border border-slate-800">
        <div>
          <h2 className="text-xl font-bold text-white flex items-center gap-2.5">
            <Bell className="w-6 h-6 text-indigo-400" />
            Notification & Reminders Hub
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Automated calendar triggers, contract expiry alerts, and payment reminders orchestrated by FinOrchestrator.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => setIsModalOpen(true)}
            className="flex items-center gap-2 px-3.5 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl transition shadow-md shadow-indigo-600/30"
          >
            <Plus className="w-4 h-4" />
            Schedule Reminder
          </button>
          <button
            onClick={loadNotifications}
            className="p-2 text-slate-400 hover:text-slate-200 hover:bg-slate-800 rounded-lg transition border border-slate-800"
            title="Refresh"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 border-b border-slate-800 pb-3">
        {[
          { id: 'ALL', label: `All (${notifications.length})` },
          { id: 'UNREAD', label: `Unread (${notifications.filter(n => !n.isRead).length})` },
          { id: 'REMINDER', label: `Reminders (${notifications.filter(n => n.type === 'REMINDER').length})` },
          { id: 'ALERT', label: `Financial Alerts (${notifications.filter(n => n.type !== 'REMINDER').length})` },
        ].map(tab => (
          <button
            key={tab.id}
            onClick={() => setFilter(tab.id as any)}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition ${
              filter === tab.id
                ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                : 'bg-slate-900 text-slate-400 hover:bg-slate-800 hover:text-slate-200 border border-slate-800'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Notifications List */}
      {loading ? (
        <div className="flex justify-center py-12">
          <RefreshCw className="w-8 h-8 animate-spin text-indigo-400" />
        </div>
      ) : filteredNotifications.length === 0 ? (
        <div className="bg-slate-900 rounded-2xl border border-slate-800 p-12 text-center text-slate-400">
          <Bell className="w-12 h-12 text-slate-700 mx-auto mb-3" />
          <h3 className="text-base font-semibold text-slate-300">No notifications found</h3>
          <p className="text-xs text-slate-500 mt-1">There are no items matching the selected filter criteria.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {filteredNotifications.map(item => (
            <div
              key={item.id}
              className={`p-5 rounded-2xl border transition flex items-start justify-between gap-4 ${
                item.isRead
                  ? 'bg-slate-900/60 border-slate-800/80 opacity-70'
                  : 'bg-slate-900 border-indigo-500/30 shadow-md shadow-indigo-950/20'
              }`}
            >
              <div className="flex items-start gap-4 flex-1">
                <div className="p-2.5 rounded-xl bg-slate-800 border border-slate-700/60 mt-0.5">
                  {getTypeIcon(item.type)}
                </div>

                <div className="space-y-1 flex-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    {!item.isRead && (
                      <span className="w-2 h-2 rounded-full bg-indigo-400 animate-pulse"></span>
                    )}
                    <h4 className="font-bold text-white text-xs tracking-wide">{item.title}</h4>
                    {getPriorityBadge(item.priority)}
                    <span className="text-[10px] font-mono text-slate-500">#{item.type}</span>
                  </div>

                  <p className="text-xs text-slate-300 leading-relaxed mt-1">{item.message}</p>

                  <div className="flex items-center gap-4 text-[11px] text-slate-500 pt-2 font-mono">
                    <span className="flex items-center gap-1.5">
                      <Calendar className="w-3 h-3 text-slate-400" />
                      Trigger: {new Date(item.triggerTime).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })}
                    </span>
                    <span className="flex items-center gap-1.5">
                      <Clock className="w-3 h-3 text-slate-400" />
                      Created: {new Date(item.createdAt).toLocaleDateString('en-IN', { month: 'short', day: 'numeric' })}
                    </span>
                  </div>
                </div>
              </div>

              {/* Actions */}
              <div className="flex items-center gap-2 flex-shrink-0">
                {!item.isRead && (
                  <button
                    onClick={() => handleMarkRead(item.id)}
                    className="p-1.5 text-xs text-slate-300 hover:text-emerald-300 hover:bg-emerald-950/40 rounded-lg transition border border-slate-700 flex items-center gap-1"
                    title="Mark as read"
                  >
                    <Check className="w-3.5 h-3.5 text-emerald-400" />
                    Read
                  </button>
                )}
                <button
                  onClick={() => handleCancel(item.id)}
                  className="p-1.5 text-xs text-slate-400 hover:text-red-400 hover:bg-red-950/40 rounded-lg transition border border-slate-700"
                  title="Delete/Dismiss"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Schedule Reminder Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 rounded-2xl max-w-md w-full p-6 shadow-2xl border border-slate-800 space-y-4 text-white">
            <div className="flex justify-between items-center pb-3 border-b border-slate-800">
              <h3 className="font-bold text-white text-base flex items-center gap-2">
                <Clock className="w-5 h-5 text-indigo-400" />
                Schedule New Reminder
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateReminder} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Reminder Title</label>
                <input
                  type="text"
                  required
                  placeholder="e.g., Pay Quarterly Advance Tax"
                  value={formData.title}
                  onChange={e => setFormData({ ...formData, title: e.target.value })}
                  className="w-full text-xs bg-slate-950 border border-slate-800 rounded-xl p-3 focus:border-indigo-500 outline-none text-white placeholder-slate-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Details / Description</label>
                <textarea
                  rows={3}
                  required
                  placeholder="Provide instructions or amounts..."
                  value={formData.message}
                  onChange={e => setFormData({ ...formData, message: e.target.value })}
                  className="w-full text-xs bg-slate-950 border border-slate-800 rounded-xl p-3 focus:border-indigo-500 outline-none text-white placeholder-slate-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Trigger Date & Time</label>
                  <input
                    type="datetime-local"
                    required
                    value={formData.triggerTime}
                    onChange={e => setFormData({ ...formData, triggerTime: e.target.value })}
                    className="w-full text-xs bg-slate-950 border border-slate-800 rounded-xl p-2.5 focus:border-indigo-500 outline-none text-white"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Priority</label>
                  <select
                    value={formData.priority}
                    onChange={e => setFormData({ ...formData, priority: e.target.value })}
                    className="w-full text-xs bg-slate-950 border border-slate-800 rounded-xl p-2.5 focus:border-indigo-500 outline-none text-white"
                  >
                    <option value="LOW">Low</option>
                    <option value="NORMAL">Normal</option>
                    <option value="HIGH">High</option>
                  </select>
                </div>
              </div>

              <div className="pt-3 flex justify-end gap-3 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 text-xs font-semibold text-slate-400 bg-slate-800 hover:bg-slate-700 rounded-xl transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-500 rounded-xl transition shadow-md shadow-indigo-600/30"
                >
                  Save Reminder
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

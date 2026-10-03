import { motion } from 'framer-motion';
import { Pill, Plus, Search, Filter, AlertTriangle } from 'lucide-react';

const Medications = () => {
  const container = {
    hidden: { opacity: 0 },
    show: { opacity: 1, transition: { staggerChildren: 0.1 } }
  };
  const item = {
    hidden: { opacity: 0, y: 20 },
    show: { opacity: 1, y: 0 }
  };

  const medications = [
    { id: 1, name: 'Lisinopril', dosage: '10mg', frequency: 'Once daily', stock: 12, refillNeeded: false },
    { id: 2, name: 'Metformin', dosage: '500mg', frequency: 'Twice daily', stock: 4, refillNeeded: true },
    { id: 3, name: 'Atorvastatin', dosage: '20mg', frequency: 'Once daily at night', stock: 45, refillNeeded: false },
    { id: 4, name: 'Amlodipine', dosage: '5mg', frequency: 'Once daily', stock: 8, refillNeeded: true },
  ];

  return (
    <motion.div initial="hidden" animate="show" exit="hidden" variants={container}>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <motion.h2 variants={item}>My Medications</motion.h2>
          <motion.p variants={item}>Manage your prescriptions and stock levels.</motion.p>
        </div>
        <motion.button variants={item} className="btn-primary">
          <Plus size={20} />
          <span>Add Medication</span>
        </motion.button>
      </div>

      <motion.div variants={item} className="glass-card" style={{ padding: '1.5rem', marginBottom: '2rem' }}>
        <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem' }}>
          <div className="search-bar" style={{ flex: 1 }}>
            <input type="text" placeholder="Search your medications..." style={{ width: '100%' }} />
          </div>
          <button className="btn-secondary">
            <Filter size={18} />
            <span>Filter</span>
          </button>
        </div>

        <div className="med-list">
          {medications.map((med) => (
            <div key={med.id} className="med-item" style={{ padding: '1.25rem' }}>
              <div className="med-info">
                <div className="med-icon">
                  <Pill size={24} />
                </div>
                <div className="med-details">
                  <h4 style={{ fontSize: '1.125rem', marginBottom: '0.25rem' }}>{med.name} <span style={{ color: 'var(--text-secondary)', fontWeight: 400, fontSize: '0.875rem' }}>- {med.dosage}</span></h4>
                  <p>{med.frequency}</p>
                </div>
              </div>
              
              <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
                <div style={{ textAlign: 'right' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', justifyContent: 'flex-end' }}>
                    {med.refillNeeded && <AlertTriangle size={16} className="text-warning" />}
                    <span style={{ fontWeight: 600, color: med.refillNeeded ? 'var(--warning)' : 'var(--text-primary)' }}>
                      {med.stock} pills left
                    </span>
                  </div>
                  <div className="stat-label">Stock Level</div>
                </div>
                
                <button className={med.refillNeeded ? "btn-primary" : "btn-secondary"} style={{ padding: '0.5rem 1rem' }}>
                  {med.refillNeeded ? 'Request Refill' : 'Edit'}
                </button>
              </div>
            </div>
          ))}
        </div>
      </motion.div>
    </motion.div>
  );
};

export default Medications;
